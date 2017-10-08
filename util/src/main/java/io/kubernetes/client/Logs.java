/*
Copyright 2017 The Kubernetes Authors.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package io.kubernetes.client;

import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.util.WebSockets;
import io.kubernetes.client.util.WebSocketStreamHandler;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Reader;

import org.apache.commons.lang.StringUtils;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Response;

/**
 * Utility class offering streaming access to Pod logs.
 */
public class Logs {
    private ApiClient apiClient;
    private CoreV1Api coreClient;

    /**
     * Simple Logs API constructor, uses default configuration
     */
    public Logs() {
        this(Configuration.getDefaultApiClient());
    }

    /**
     * Logs API Constructor
     * @param apiClient The api client to use.
     */
    public Logs(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.coreClient = new CoreV1Api(apiClient);
    }

    /**
     * Get the API client for these Logs operations.
     * @return The API client that will be used.
     */
    public ApiClient getApiClient() {
        return apiClient;
    }

    /**
     * Set the API client for subsequent Logs operations.
     * @param apiClient The new API client to use.
     */
    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public InputStream streamNamespacedPodLog(V1Pod pod) throws ApiException, IOException {
        return streamNamespacedPodLog(pod.getMetadata().getNamespace(), pod.getMetadata().getName(),
                                      pod.getSpec().getContainers().get(0).getName());
    }

    // Important note. You must close this stream or else you can leak connections.
    public InputStream streamNamespacedPodLog(String namespace, String name, String container) throws ApiException, IOException {
        return streamNamespacedPodLog(namespace, name, container, null, null, false);
    }


    // Important note. You must close this stream or else you can leak connections.
    public InputStream streamNamespacedPodLog(String namespace, String name, String container,
                                              Integer sinceSeconds, Integer tailLines, boolean timestamps) throws ApiException, IOException {
        Call call = coreClient.readNamespacedPodLogCall(name, namespace, container, true, null, "false", false, sinceSeconds, tailLines, timestamps, null, null);
        Response response = call.execute();
        if (!response.isSuccessful()) {
            throw new ApiException("Logs request failed: " + response.code());
        }
        return response.body().byteStream();
    }
}