/**
 * Copyright (c) 2011 Yahoo! Inc. All rights reserved.
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
 * limitations under the License. See accompanying LICENSE file.
 */

package com.yahoo.paxos.client;

public class SimpleClient implements ClientInterface {
    
    private PaxosInterface paxos;
    private byte[] payload;
    
    public SimpleClient(int requestSize) {
        payload = new byte[requestSize];
    }

    @Override
    public void connected() {
        paxos.submitNewRequest(payload);
        
    }
    @Override
    public void messageReceived(byte[] message) {
        paxos.submitNewRequest(payload);
    }
    
    public void setInterface(PaxosInterface paxosInterface) {
        paxos = paxosInterface;
    };
}
