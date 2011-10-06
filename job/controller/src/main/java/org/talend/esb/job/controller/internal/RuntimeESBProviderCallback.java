/*
 * #%L
 * Talend :: ESB :: Job :: Controller
 * %%
 * Copyright (C) 2011 Talend Inc.
 * %%
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
 * #L%
 */
package org.talend.esb.job.controller.internal;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import routines.system.api.ESBJobInterruptedException;
import routines.system.api.ESBProviderCallback;

public class RuntimeESBProviderCallback implements ESBProviderCallback {

    private static final MessageExchange POISON = new MessageExchange(null);
    
    private final boolean isRequestResponse;
    private final BlockingQueue<MessageExchange> requests = new LinkedBlockingQueue<MessageExchange>();

    private volatile MessageExchange currentExchange;

    private volatile boolean stopped;

    public RuntimeESBProviderCallback(boolean isRequestResponse) {
        this.isRequestResponse = isRequestResponse;
    }

    public Object getRequest() throws ESBJobInterruptedException {
        currentExchange = null;
        while (currentExchange == null) {
            try {
                currentExchange = requests.take();
                if (currentExchange == POISON) {
                    stopped = true;
                    throw new ESBJobInterruptedException("Job was cancelled.");
                }
            } catch (InterruptedException e) {
                prepareStop();
            }
        }
        return currentExchange.request;
    }

    public void sendResponse(Object response) {
        currentExchange.response = response;
        synchronized (currentExchange) {
            currentExchange.notify();
        }
    }

    public Object invoke(Object payload) throws Exception {
        MessageExchange myExchange = new MessageExchange(payload);
        requests.put(myExchange);
        if(!isRequestResponse) {
            return null;
        }
        synchronized (myExchange) {
            myExchange.wait();
        }
        return myExchange.response;
    }
    
    public void stop() {
        prepareStop();
    }

    public boolean isStopped() {
        return stopped;    
    }
    
    protected void prepareStop() {
        boolean success = false;
        while(! success) {
            try {
                requests.put(POISON);
                success = true;
            } catch(InterruptedException e) {
            }
        }
    }
    
    private static class MessageExchange {
        public Object request;
        
        public Object response;
        
        public MessageExchange(Object request) {
            this.request = request;
        }
    }
}
