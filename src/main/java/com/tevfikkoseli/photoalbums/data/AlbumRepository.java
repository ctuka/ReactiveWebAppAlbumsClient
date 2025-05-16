package com.tevfikkoseli.photoalbums.data;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface AlbumRepository extends ReactiveCrudRepository<AlbumEntity, UUID> {
    Flux<AlbumEntity> findByUserId(String userId);
    Mono<AlbumEntity> findByIdAndUserId(UUID id, String userId);
    Mono<Void> deleteByIdAndUserId(UUID id, String userId);
}
