/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package net.iowntheinter.kvdn.gremlin.jsr223;

import org.apache.tinkerpop.gremlin.jsr223.AbstractGremlinPlugin;
import org.apache.tinkerpop.gremlin.jsr223.DefaultImportCustomizer;
import org.apache.tinkerpop.gremlin.jsr223.ImportCustomizer;
import net.iowntheinter.kvdn.gremlin.*;

/**
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public final class KVDNGraphGremlinPlugin extends AbstractGremlinPlugin {
    private static final String NAME = "KVDNpop.KVDNgraph";

    private static final ImportCustomizer imports = DefaultImportCustomizer.build()
            .addClassImports(KVDNEdge.class,
                    KVDNElement.class,
                    //                 KVDNFactory.class,
                    KVDNGraph.class,
                    KVDNGraphVariables.class,
                    KVDNHelper.class,
                    KVDNIoRegistryV3d0.class,
                    KVDNProperty.class,
                    KVDNVertex.class,
                    KVDNVertexProperty.class
//                    KVDNGraphComputer.class,
//                    KVDNGraphComputerView.class,
//                    KVDNMapEmitter.class,
//                    KVDNMemory.class,
//                    KVDNMessenger.class,
//                    KVDNReduceEmitter.class,
//                    KVDNWorkerPool.class
            ).create();

    private static final KVDNGraphGremlinPlugin instance = new KVDNGraphGremlinPlugin();

    public KVDNGraphGremlinPlugin() {
        super(NAME, imports);
    }

    public static KVDNGraphGremlinPlugin instance() {
        return instance;
    }
}