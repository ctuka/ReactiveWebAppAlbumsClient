package com.tevfikkoseli.photoalbums.presentation;


import com.tevfikkoseli.photoalbums.service.AlbumsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/albums")
public class AlbumsController {

    private final AlbumsService albumsService;

    public AlbumsController(AlbumsService albumsService) {
        this.albumsService = albumsService;
    }

    @GetMapping
    public Flux<AlbumRest> getAlbums(Principal principal) {
        return albumsService.getAlbums(principal.getName())
                .filter(albumRest -> albumRest.getUserId().equals(principal.getName()));
    }

    @PostMapping
    public Mono<ResponseEntity<AlbumRest>> createAlbum(@Valid @RequestBody Mono<AlbumRest> album,
            Principal principal) {
        return album.map(albumRest -> {
            albumRest.setUserId(principal.getName());
            return albumRest;
        }).flatMap(albumRest -> albumsService.createAlbum(Mono.just(albumRest)))
                .map(albumRest -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .location(URI.create("/albums/" + albumRest.getId()))
                        .body(albumRest));
    }

//    @PostMapping
//    public Mono<ResponseEntity<AlbumRest>> createAlbum(@Valid @RequestBody Mono<AlbumRest> album,
//                                                       @RequestHeader (name = "Authorization") String jwt) {
//        return albumsService.createAlbum(album, jwt)
//                .map(albumRest -> ResponseEntity
//                        .status(HttpStatus.CREATED)
//                        .location(URI.create("/albums/" + albumRest.getId()))
//                        .body(albumRest));
//    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<AlbumRest>> getAlbum(@PathVariable UUID id, Principal principal) {
        return albumsService.getAlbum(id, principal.getName())
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<AlbumRest>> updateAlbum(@PathVariable UUID id, 
                                                       @Valid @RequestBody Mono<AlbumRest> album,
                                                       Principal principal) {
        return album.flatMap(albumRest -> {
            albumRest.setId(id);
            albumRest.setUserId(principal.getName());
            return albumsService.updateAlbum(Mono.just(albumRest));
        }).map(ResponseEntity::ok)
          .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteAlbum(@PathVariable UUID id, Principal principal) {
        return albumsService.deleteAlbum(id, principal.getName())
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

}
