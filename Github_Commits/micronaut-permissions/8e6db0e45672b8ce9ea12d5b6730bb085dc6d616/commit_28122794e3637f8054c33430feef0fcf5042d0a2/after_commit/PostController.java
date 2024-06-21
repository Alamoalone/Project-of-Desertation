/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2023 Agorapulse.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.agorapulse.permissions;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.hateoas.JsonError;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Controller("/post")
public class PostController {

    private final PostService postService;
    private final PostRepository postRepository;

    public PostController(PostService postService, PostRepository postRepository) {
        this.postService = postService;
        this.postRepository = postRepository;
    }

    @Status(HttpStatus.CREATED)
    @io.micronaut.http.annotation.Post("/")
    public Post create(@Nullable @Header("X-User-Id") Long userId, String message) {
        return postRepository.save(postService.create(userId, message));
    }

    @Delete("/{id}")
    public Post archive(Long id) {
        return postRepository.save(postService.archive(postRepository.get(id)));
    }

    @Put("/{id}")
    public Post publish(Long id) {
        return postRepository.save(postService.publish(postRepository.get(id)));
    }

    @Status(HttpStatus.CREATED)
    @io.micronaut.http.annotation.Post("/merge")
    public Post merge(@Nullable @Header("X-User-Id") Long userId, @Body PostMergeRequest postMergeRequest) {
        Post mergedPost = postService.merge(userId,
            postRepository.get(postMergeRequest.getId1()),
            postRepository.get(postMergeRequest.getId2()));
        return postRepository.save(mergedPost);
    }

    @io.micronaut.http.annotation.Post("/handle-collection")
    public void handleCollection(@Body HandleCollectionRequest handleCollectionRequest) {
        List<Post> posts = handleCollectionRequest.getIds() != null ? handleCollectionRequest.getIds()
            .stream()
            .map(postRepository::get)
            .collect(Collectors.toList()) : null;
        postService.handleCollection(posts);
    }

    // tag::error[]
    @Error(PermissionException.class)
    public HttpResponse<JsonError> permissionException(PermissionException ex) {
        return HttpResponse.<JsonError>unauthorized().body(new JsonError(ex.getMessage()));
    }
    // end::error[]

    @Error(IllegalArgumentException.class)
    public HttpResponse<JsonError> illegalArgumentException(IllegalArgumentException ex) {
        return HttpResponse.badRequest(new JsonError(ex.getMessage()));
    }

}
