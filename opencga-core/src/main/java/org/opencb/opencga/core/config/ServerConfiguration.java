/*
 * Copyright 2015-2017 OpenCB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opencb.opencga.core.config;

/**
 * Created by pfurio on 04/01/17.
 */
public class ServerConfiguration {

    private RestServerConfiguration rest;
    private GrpcServerConfiguration grpc;

    public ServerConfiguration() {
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ServerConfiguration{");
        sb.append("rest=").append(rest);
        sb.append(", grpc=").append(grpc);
        sb.append('}');
        return sb.toString();
    }

    public RestServerConfiguration getRest() {
        return rest;
    }

    public ServerConfiguration setRest(RestServerConfiguration rest) {
        this.rest = rest;
        return this;
    }

    public GrpcServerConfiguration getGrpc() {
        return grpc;
    }

    public ServerConfiguration setGrpc(GrpcServerConfiguration grpc) {
        this.grpc = grpc;
        return this;
    }
}
