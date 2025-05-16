package com.tevfikkoseli.photoalbums.service;


import com.tevfikkoseli.photoalbums.data.AlbumEntity;
import com.tevfikkoseli.photoalbums.data.AlbumRepository;
import com.tevfikkoseli.photoalbums.presentation.AlbumRest;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

@Service
public class AlbumsServiceImpl implements AlbumsService {

    AlbumRepository albumRepository;


    public AlbumsServiceImpl(AlbumRepository albumRepository) {
        this.albumRepository = albumRepository;
    }

    @Override
    public Flux<AlbumRest> getAlbums(String userId) {
        return albumRepository
                .findByUserId(userId)
                .map(albumEntity -> convertToRest(albumEntity));
    }

    @Override
    public Mono<AlbumRest> createAlbum(Mono<AlbumRest> albumMono) {
        return albumMono
                .flatMap(this::convertToEntity)
                .flatMap(albumRepository::save)
                .mapNotNull(this::convertToRest);
    }

    @Override
    public Mono<AlbumRest> getAlbum(UUID id, String userId) {
        return albumRepository.findByIdAndUserId(id, userId)
                .map(this::convertToRest);
    }

    @Override
    public Mono<AlbumRest> updateAlbum(Mono<AlbumRest> albumMono) {
        return albumMono.flatMap(album -> 
            albumRepository.findByIdAndUserId(album.getId(), album.getUserId())
                    .flatMap(existingAlbum -> {
                        existingAlbum.setTitle(album.getTitle());
                        return albumRepository.save(existingAlbum);
                    })
                    .map(this::convertToRest)
        );
    }

    @Override
    public Mono<Void> deleteAlbum(UUID id, String userId) {
        return albumRepository.deleteByIdAndUserId(id, userId);
    }
 
    private Mono<AlbumEntity> convertToEntity(AlbumRest album) {
        return Mono.fromCallable(() -> {
            AlbumEntity albumEntity = new AlbumEntity();
            BeanUtils.copyProperties(album, albumEntity);
            return albumEntity;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private AlbumRest convertToRest(AlbumEntity albumEntity) {
        AlbumRest albumRest = new AlbumRest();
        BeanUtils.copyProperties(albumEntity, albumRest);
        return albumRest;
    }

}