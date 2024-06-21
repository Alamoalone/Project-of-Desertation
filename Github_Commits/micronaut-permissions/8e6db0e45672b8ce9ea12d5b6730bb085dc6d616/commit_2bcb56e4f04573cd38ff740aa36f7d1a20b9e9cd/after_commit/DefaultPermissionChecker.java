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

import io.micronaut.core.order.OrderUtil;
import io.micronaut.core.type.Argument;

import javax.inject.Singleton;
import java.util.List;

@Singleton
public class DefaultPermissionChecker implements PermissionChecker {

    private final List<PermissionAdvisor<?>> advisors;

    public DefaultPermissionChecker(List<PermissionAdvisor<?>> advisors) {
        OrderUtil.sort(advisors);
        this.advisors = advisors;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> PermissionCheckResult checkPermission(String permissionDefinition, T value, Argument<T> valueType) {
        if (valueType.isContainerType() && valueType.hasTypeVariables()) {
            return checkPermissionOnContainer(permissionDefinition, (Iterable<Object>) value, (Argument<Iterable<Object>>) valueType);
        }

        boolean permissionDenied = false;
        for (PermissionAdvisor<?> advisor : advisors) {
            if (advisor.argumentType().getType().isAssignableFrom(valueType.getType())) {
                PermissionAdvisor<Object> typeSafeAdvisor = (PermissionAdvisor<Object>) advisor;
                PermissionCheckResult permissionCheckResult = typeSafeAdvisor.checkPermissions(permissionDefinition, value, (Argument<Object>) valueType);
                switch (permissionCheckResult) {
                    case ALLOW:
                        return PermissionCheckResult.ALLOW;
                    case DENY:
                        permissionDenied = true;
                    case UNKNOWN:
                    default:
                        // continue to the next advisor
                }
            }
        }

        return permissionDenied ? PermissionCheckResult.DENY : PermissionCheckResult.UNKNOWN;
    }

    @SuppressWarnings("unchecked")
    public <T> PermissionCheckResult checkPermissionOnContainer(String permissionDefinition, Iterable<T> value, Argument<Iterable<T>> valueType) {
        if (value == null) {
            return PermissionCheckResult.ALLOW;
        }
        for (T item : value) {
            switch (checkPermission(permissionDefinition, item, valueType.getTypeParameters()[0])) {
                case DENY:
                    return PermissionCheckResult.DENY;
                case UNKNOWN:
                    return PermissionCheckResult.UNKNOWN;
                case ALLOW:
                default:
                    // proceed to next item
            }
        }

        return PermissionCheckResult.ALLOW;
    }
}
