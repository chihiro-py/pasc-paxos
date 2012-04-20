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

package com.yahoo.paxos.handlers.acceptor;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yahoo.pasc.Message;
import com.yahoo.paxos.handlers.PaxosHandler;
import com.yahoo.paxos.handlers.proposer.ProposerRequest;
import com.yahoo.paxos.messages.Accept;
import com.yahoo.paxos.messages.Accepted;
import com.yahoo.paxos.messages.PaxosDescriptor;
import com.yahoo.paxos.state.ClientTimestamp;
import com.yahoo.paxos.state.IidAcceptorsCounts;
import com.yahoo.paxos.state.IidRequest;
import com.yahoo.paxos.state.PaxosState;

public class AcceptorAccept extends PaxosHandler<Accept> {

    private static final Logger LOG = LoggerFactory.getLogger(ProposerRequest.class);

    @Override
    public boolean guardPredicate(Message receivedMessage) {
        return receivedMessage instanceof Accept;
    }

    @Override
    public List<PaxosDescriptor> processMessage(Accept message, PaxosState state) {
        if (state.getIsLeader()) return null;
        
        int ballot = message.getBallot();
        int currentBallot = state.getBallotAcceptor();
        List<PaxosDescriptor> descriptors = new ArrayList<PaxosDescriptor>();
        if (ballot < currentBallot) {
            LOG.trace("Rejecting accept. msg ballot: {} current ballot: {}", ballot,
                    currentBallot);
            // We promised not to accept ballots lower than our current ballot
            return null;
        }
        long iid = message.getIid();
        long firstInstanceId = state.getFirstInstanceId();
        
        if (firstInstanceId <= iid && iid < firstInstanceId + state.getMaxInstances()) {
//            LOG.trace("Received valid accept: {} ", message);
            
            ClientTimestamp [] cts = message.getValues();
            byte[][] requests = message.getRequests();
            int arraySize = message.getArraySize();
            int countReceivedRequests = 0;
            for (int i = 0; i < arraySize; ++i) {
                if (requests != null && requests[i] != null) {
                    state.setReceivedRequest(cts[i], new IidRequest(iid, requests[i]));
                    countReceivedRequests++;
                } else {
                    IidRequest request = state.getReceivedRequest(cts[i]);
                    if (request == null || (request.getIid() != -1 && request.getIid() < firstInstanceId)) {
    //                    LOG.trace("Creating new request (Old one: {}) ", request);
                        request = new IidRequest(iid);
                        state.setReceivedRequest(cts[i], request);
                    } else if (request.getRequest() != null && request.getIid() == -1) {
                        request.setIid(iid);
                        countReceivedRequests++;
                    } else {
                        LOG.warn("The acceptor created this request. Duplicated accept?");
                    }
                }
            }
            IidAcceptorsCounts accepted = state.getAcceptedElement(iid);
            if (accepted == null || accepted.getIid() != iid) {
//                LOG.trace("Initializing accepted for iid {}", iid);
                accepted = new IidAcceptorsCounts(iid);
                state.setAcceptedElement(iid, accepted);
            }
            accepted.setReceivedRequests(countReceivedRequests);
            accepted.setTotalRequests(arraySize);
            state.setInstancesElement(iid, message.getInstance());
            
            checkAccept(iid, accepted, state, descriptors);
            
            return descriptors;
        } else {
//            LOG.warn("Received invalid accept: {} iid: {} firstIid: {} maxInst: {}", new Object[] { message, iid, firstInstanceId, state.getMaxInstances() });
        }
        return null;
    }

    public static void checkAccept(long iid, PaxosState state, List<PaxosDescriptor> descriptors) {
        checkAccept(iid, null, state, descriptors);
    }
    
    public static void checkAccept(long iid, IidAcceptorsCounts accepted, PaxosState state, List<PaxosDescriptor> descriptors) {
        if (accepted == null) {
            // Called from proposer, a new request has been received
            accepted = state.getAcceptedElement(iid);
            accepted.setReceivedRequests(accepted.getReceivedRequests() + 1);
        }

        int receivedRequests = accepted.getReceivedRequests();
        int totalRequests = accepted.getTotalRequests();
        if (receivedRequests == totalRequests) {
            accepted.setAccepted(true);
            
            descriptors.add(new Accepted.Descriptor(iid));
            
//            Learner.checkExecute(iid, state, descriptors);
//        } else {
//          LOG.trace("Only received {} requests but need {} to accept this instance", receivedRequests,
//                  totalRequests);
        }
    }
}
