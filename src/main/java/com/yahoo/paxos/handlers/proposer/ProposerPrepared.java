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

package com.yahoo.paxos.handlers.proposer;

import java.util.List;

import com.yahoo.pasc.Message;
import com.yahoo.paxos.handlers.PaxosHandler;
import com.yahoo.paxos.messages.PaxosDescriptor;
import com.yahoo.paxos.messages.Prepared;
import com.yahoo.paxos.state.PaxosState;

public class ProposerPrepared extends PaxosHandler<Prepared> {

//   private static final Log LOG = LogFactory.getLog(ProposerPrepared.class);

   @Override
   public boolean guardPredicate(Message receivedMessage) {
      return receivedMessage instanceof Prepared;
   }

   @Override
   public List<PaxosDescriptor> processMessage(Prepared receivedMessage, PaxosState state) {
       return null;
   }

}
