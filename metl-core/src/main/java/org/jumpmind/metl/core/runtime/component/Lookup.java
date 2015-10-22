/**
 * Licensed to JumpMind Inc under one or more contributor
 * license agreements.  See the NOTICE file distributed
 * with this work for additional information regarding
 * copyright ownership.  JumpMind Inc licenses this file
 * to you under the GNU General Public License, version 3.0 (GPLv3)
 * (the "License"); you may not use this file except in compliance
 * with the License.
 *
 * You should have received a copy of the GNU General Public License,
 * version 3.0 (GPLv3) along with this library; if not, see
 * <http://www.gnu.org/licenses/>.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jumpmind.metl.core.runtime.component;

import static org.apache.commons.lang.StringUtils.isBlank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jumpmind.metl.core.runtime.EntityData;
import org.jumpmind.metl.core.runtime.Message;
import org.jumpmind.metl.core.runtime.flow.ISendMessageCallback;
import org.jumpmind.properties.TypedProperties;

public class Lookup extends AbstractComponentRuntime {
    
    public final static String TYPE = "Lookup";

    public final static String SOURCE_STEP = "lookup.data.source.step";
    public final static String LOOKUP_KEY = "lookup.key.attribute";
    public final static String LOOKUP_VALUE = "lookup.value.attribute";
    public final static String REPLACEMENT_VALUE_ATTRIBUTE = "replacement.value.attribute";
    public final static String REPLACEMENT_KEY_ATTRIBUTE = "replacement.key.attribute";

    boolean lookupInitialized = false;

    String sourceStepId;
    String keyAttributeId;
    String valueAttributeId;
    String replacementKeyAttributeId;
    String replacementValueAttributeId;

    Map<Object, Object> lookup = new HashMap<Object, Object>();

    List<Message> queuedWhileWaitingForLookup = new ArrayList<Message>();

    @Override
    protected void start() {
        lookupInitialized = false;
        TypedProperties properties = getTypedProperties();
        sourceStepId = properties.get(SOURCE_STEP);
        keyAttributeId = properties.get(LOOKUP_KEY);
        valueAttributeId = properties.get(LOOKUP_VALUE);
        replacementKeyAttributeId = properties.get(REPLACEMENT_KEY_ATTRIBUTE);
        replacementValueAttributeId = properties.get(REPLACEMENT_VALUE_ATTRIBUTE);

        if (isBlank(sourceStepId) || getFlow().findFlowStepWithId(sourceStepId) == null) {
            throw new IllegalStateException("The source step must be specified");
        }
    }
    
    @Override
    public boolean supportsStartupMessages() {
        return false;
    }

    @Override
    public void handle(Message inputMessage, ISendMessageCallback callback, boolean unitOfWorkBoundaryReached) {
        if (sourceStepId.equals(inputMessage.getHeader().getOriginatingStepId())) {
            List<EntityData> datas = inputMessage.getPayload();
            for (EntityData entityData : datas) {
                lookup.put(entityData.get(keyAttributeId), entityData.get(valueAttributeId));
            }
            lookupInitialized = inputMessage.getHeader().isUnitOfWorkLastMessage();

            if (lookupInitialized) {
                Iterator<Message> messages = queuedWhileWaitingForLookup.iterator();
                while (messages.hasNext()) {
                    Message message = messages.next();
                    enhanceAndSend(message, callback, unitOfWorkBoundaryReached);
                }
            }
        } else if (!lookupInitialized) {
            queuedWhileWaitingForLookup.add(inputMessage);
        } else if (lookupInitialized) {
            enhanceAndSend(inputMessage, callback, unitOfWorkBoundaryReached);
        }
    }

    protected void enhanceAndSend(Message message, ISendMessageCallback callback, boolean unitOfWorkLastMessage) {

        debug("Using lookup table: {}", lookup);

        ArrayList<EntityData> payload = new ArrayList<EntityData>();

        List<EntityData> datas = message.getPayload();
        for (int j = 0; j < datas.size(); j++) {
            getComponentStatistics().incrementNumberEntitiesProcessed(threadNumber);
            EntityData oldData = datas.get(j);
            EntityData newData = new EntityData();
            newData.putAll(oldData);
            newData.put(replacementValueAttributeId, lookup.get(oldData.get(replacementKeyAttributeId)));
            payload.add(newData);
        }

        callback.sendMessage(null, payload, unitOfWorkLastMessage);
    }

}