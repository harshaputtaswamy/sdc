/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.be.dao.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SocketOptions;
import com.datastax.driver.core.policies.ConstantReconnectionPolicy;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.datastax.driver.core.policies.LoadBalancingPolicy;
import com.datastax.driver.core.policies.TokenAwarePolicy;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.Mapper.Option;
import com.datastax.driver.mapping.MappingManager;
import fj.data.Either;
import java.util.List;
import javax.annotation.PreDestroy;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Component;

@Component("cassandra-client")
public class CassandraClient {

    private static Logger logger = Logger.getLogger(CassandraClient.class.getName());
    private Cluster cluster;
    private boolean isConnected;

    public CassandraClient() {
        super();
        isConnected = false;
        List<String> cassandraHosts = null;
        try {
            cassandraHosts = ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig().getCassandraHosts();
            Integer cassandraPort = ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig().getCassandraPort();
            Long reconnectTimeout = ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig().getReconnectTimeout();
            logger.debug("creating cluster to hosts:{} port:{} with reconnect timeout:{}", cassandraHosts, cassandraPort, reconnectTimeout);
            Cluster.Builder clusterBuilder = Cluster.builder().withReconnectionPolicy(new ConstantReconnectionPolicy(reconnectTimeout))
                .withRetryPolicy(DefaultRetryPolicy.INSTANCE);
            cassandraHosts.forEach(host -> clusterBuilder.addContactPoint(host).withPort(cassandraPort));
            setSocketOptions(clusterBuilder);
            enableAuthentication(clusterBuilder);
            enableSsl(clusterBuilder);
            setLocalDc(clusterBuilder);
            cluster = clusterBuilder.build();
            isConnected = true;
        } catch (Exception e) {
            logger.info("** CassandraClient isn't connected to {}", cassandraHosts);
        }
        logger.info("** CassandraClient created");
    }

    private void setSocketOptions(Cluster.Builder clusterBuilder) {
        SocketOptions socketOptions = new SocketOptions();
        Integer socketConnectTimeout = ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig()
            .getSocketConnectTimeout();
        if (socketConnectTimeout != null) {
            logger.info("SocketConnectTimeout was provided, setting Cassandra client to use SocketConnectTimeout: {} .", socketConnectTimeout);
            socketOptions.setConnectTimeoutMillis(socketConnectTimeout);
        }
        Integer socketReadTimeout = ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig().getSocketReadTimeout();
        if (socketReadTimeout != null) {
            logger.info("SocketReadTimeout was provided, setting Cassandra client to use SocketReadTimeout: {} .", socketReadTimeout);
            socketOptions.setReadTimeoutMillis(socketReadTimeout);
        }
        clusterBuilder.withSocketOptions(socketOptions);
    }

    private void setLocalDc(Cluster.Builder clusterBuilder) {
        String localDataCenter = ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig().getLocalDataCenter();
        if (localDataCenter != null) {
            logger.info("localDatacenter was provided, setting Cassndra clint to use datacenter: {} as local.", localDataCenter);
            LoadBalancingPolicy tokenAwarePolicy = new TokenAwarePolicy(DCAwareRoundRobinPolicy.builder().withLocalDc(localDataCenter).build());
            clusterBuilder.withLoadBalancingPolicy(tokenAwarePolicy);
        } else {
            logger.info(
                "localDatacenter was provided,  the driver will use the datacenter of the first contact point that was reached at initialization");
        }
    }

    private void enableSsl(Cluster.Builder clusterBuilder) {
        boolean ssl = ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig().isSsl();
        if (ssl) {
            String truststorePath = ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig().getTruststorePath();
            String truststorePassword = ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig()
                .getTruststorePassword();
            if (truststorePath == null || truststorePassword == null) {
                logger.error("ssl is enabled but truststorePath or truststorePassword were not supplied.");
            } else {
                System.setProperty("javax.net.ssl.trustStore", truststorePath);
                System.setProperty("javax.net.ssl.trustStorePassword", truststorePassword);
                clusterBuilder.withSSL();
            }
        }
    }

    private void enableAuthentication(Cluster.Builder clusterBuilder) {
        boolean authenticate = ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig().isAuthenticate();
        if (authenticate) {
            String username = ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig().getUsername();
            String password = ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig().getPassword();
            if (username == null || password == null) {
                logger.error("authentication is enabled but username or password were not supplied.");
            } else {
                clusterBuilder.withCredentials(username, password);
            }
        }
    }

    /**
     * @param keyspace - key space to connect
     * @return
     */
    public Either<ImmutablePair<Session, MappingManager>, CassandraOperationStatus> connect(String keyspace) {
        if (cluster != null) {
            try {
                Session session = cluster.connect(keyspace);
                if (session != null) {
                    MappingManager manager = new MappingManager(session);
                    return Either.left(new ImmutablePair<>(session, manager));
                } else {
                    return Either.right(CassandraOperationStatus.KEYSPACE_NOT_CONNECTED);
                }
            } catch (Throwable e) {
                logger.debug("Failed to connect to keyspace [{}], error :", keyspace, e);
                return Either.right(CassandraOperationStatus.KEYSPACE_NOT_CONNECTED);
            }
        }
        return Either.right(CassandraOperationStatus.CLUSTER_NOT_CONNECTED);
    }

    public <T> CassandraOperationStatus save(T entity, Class<T> clazz, MappingManager manager) {
        if (!isConnected) {
            return CassandraOperationStatus.CLUSTER_NOT_CONNECTED;
        }
        try {
            Mapper<T> mapper = manager.mapper(clazz);
            mapper.save(entity, Mapper.Option.saveNullFields(false));
        } catch (Exception e) {
            logger.error(EcompLoggerErrorCode.DATA_ERROR, CassandraClient.class.getName(), "Failed to save entity [{}], error :", entity, e);
            return CassandraOperationStatus.GENERAL_ERROR;
        }
        return CassandraOperationStatus.OK;
    }

    public <T> Either<T, CassandraOperationStatus> getById(String id, Class<T> clazz, MappingManager manager) {
        if (!isConnected) {
            return Either.right(CassandraOperationStatus.CLUSTER_NOT_CONNECTED);
        }
        try {
            Mapper<T> mapper = manager.mapper(clazz);            
            T result = mapper.get(id);
            if (result == null) {
                logger.info("Failed to get by Id [{}], trying again with consistency level ALL", id);
                result = mapper.get(id, Option.consistencyLevel(ConsistencyLevel.ALL));
            }
            if (result == null) {
                logger.info("Failed to get by Id [{}] with consistency level ALL", id);
                return Either.right(CassandraOperationStatus.NOT_FOUND);
            }
            return Either.left(result);
        } catch (Exception e) {
            logger.debug("Failed to get by Id [{}], error :", id, e);
            return Either.right(CassandraOperationStatus.GENERAL_ERROR);
        }
    }

    public <T> CassandraOperationStatus delete(String id, Class<T> clazz, MappingManager manager) {
        if (!isConnected) {
            return CassandraOperationStatus.CLUSTER_NOT_CONNECTED;
        }
        try {
            Mapper<T> mapper = manager.mapper(clazz);
            mapper.delete(id);
        } catch (Exception e) {
            logger.debug("Failed to delete by id [{}], error :", id, e);
            return CassandraOperationStatus.GENERAL_ERROR;
        }
        return CassandraOperationStatus.OK;
    }

    public boolean isConnected() {
        return isConnected;
    }

    @PreDestroy
    public void closeClient() {
        if (isConnected) {
            cluster.close();
        }
        logger.info("** CassandraClient cluster closed");
    }
}
