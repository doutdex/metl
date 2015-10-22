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

import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.h2.util.StringUtils;
import org.jumpmind.metl.core.model.ComponentAttributeSetting;
import org.jumpmind.metl.core.model.Model;
import org.jumpmind.metl.core.model.ModelAttribute;
import org.jumpmind.metl.core.model.ModelEntity;
import org.jumpmind.metl.core.runtime.EntityData;
import org.jumpmind.metl.core.runtime.LogLevel;
import org.jumpmind.metl.core.runtime.Message;
import org.jumpmind.metl.core.runtime.flow.ISendMessageCallback;
import org.jumpmind.properties.TypedProperties;

public class FixedLengthFormatter extends AbstractComponentRuntime {

    public static final String TYPE = "Format Fixed";

    public final static String FIXED_LENGTH_FORMATTER_WRITE_HEADER = "fixed.length.formatter.header";

    public final static String FIXED_LENGTH_FORMATTER_ATTRIBUTE_ORDINAL = "fixed.length.formatter.attribute.ordinal";
    public final static String FIXED_LENGTH_FORMATTER_ATTRIBUTE_LENGTH = "fixed.length.formatter.attribute.length";
    public final static String FIXED_LENGTH_FORMATTER_ATTRIBUTE_FORMAT_FUNCTION = "fixed.length.formatter.attribute.format.function";

    /* settings */
    boolean useHeader;

    /* other vars */
    List<AttributeFormat> attributesList;

    @Override
    protected void start() {
        TypedProperties properties = getTypedProperties();
        useHeader = properties.is(FIXED_LENGTH_FORMATTER_WRITE_HEADER);
        convertAttributeSettingsToAttributeFormat();
    }
    
    @Override
    public boolean supportsStartupMessages() {
        return false;
    }

    @Override
    public void handle( Message inputMessage, ISendMessageCallback callback, boolean unitOfWorkBoundaryReached) {
        if (attributesList == null || attributesList.size() == 0) {
            throw new IllegalStateException(
                    "There are no format attributes configured.  Writing all entity fields to the output.");
        }

        ArrayList<EntityData> inputRows = inputMessage.getPayload();

        ArrayList<String> outputPayload = new ArrayList<String>();

        if (useHeader) {
            StringBuilder stringBuilder = new StringBuilder();
            for (AttributeFormat attr : attributesList) {
                if (attr.getAttribute() != null) {
                    String name = attr.getAttribute().getName();
                    String paddedValue = StringUtils.pad(name != null ? name.toString() : "", attr.getLength(), " ", true);
                    stringBuilder.append(paddedValue);
                }
            }
            outputPayload.add(stringBuilder.toString());
        }

        String outputRec;
        for (EntityData inputRow : inputRows) {
            outputRec = processInputRow(inputRow);
            log(LogLevel.DEBUG, String.format("Generated record: %s", outputRec));
            outputPayload.add(outputRec);
        }

        callback.sendMessage(null, outputPayload, unitOfWorkBoundaryReached);
    }

    private String processInputRow(EntityData inputRow) {
        StringBuilder stringBuilder = new StringBuilder();
        for (AttributeFormat attribute : attributesList) {
            Object value = inputRow.get(attribute.getAttributeId());
            if (isNotBlank(attribute.getFormatFunction())) {
                value = ModelAttributeScriptHelper.eval(attribute.getAttribute(), value, attribute.getEntity(), 
                        inputRow, attribute.getFormatFunction());
            }
            String paddedValue = StringUtils.pad(value != null ? value.toString() : "", attribute.getLength(), " ", true);
            stringBuilder.append(paddedValue);
        }
        return stringBuilder.toString();
    }
    
    private void convertAttributeSettingsToAttributeFormat() {

        Map<String, AttributeFormat> attributesMap = new HashMap<String, AttributeFormat>();

        List<ComponentAttributeSetting> attributeSettings = getComponent().getAttributeSettings();
        for (ComponentAttributeSetting attributeSetting : attributeSettings) {
            if (!attributesMap.containsKey(attributeSetting.getAttributeId())) {
                Model inputModel = getComponent().getInputModel();
                ModelAttribute attribute = inputModel.getAttributeById(attributeSetting.getAttributeId());
                ModelEntity entity = inputModel.getEntityById(attribute.getEntityId());
                attributesMap.put(attributeSetting.getAttributeId(), new AttributeFormat(attribute, entity));
            }

            if (attributeSetting.getName().equalsIgnoreCase(FIXED_LENGTH_FORMATTER_ATTRIBUTE_ORDINAL)) {
                attributesMap.get(attributeSetting.getAttributeId()).setOrdinal(Integer.parseInt(attributeSetting.getValue()));
            } else if (attributeSetting.getName().equalsIgnoreCase(FIXED_LENGTH_FORMATTER_ATTRIBUTE_LENGTH)) {
                attributesMap.get(attributeSetting.getAttributeId()).setLength(Integer.parseInt(attributeSetting.getValue()));
            } else if (attributeSetting.getName().equalsIgnoreCase(FIXED_LENGTH_FORMATTER_ATTRIBUTE_FORMAT_FUNCTION)) {
                attributesMap.get(attributeSetting.getAttributeId()).setFormatFunction(attributeSetting.getValue());
            }
        }

        attributesList = new ArrayList<AttributeFormat>(attributesMap.values());

        Collections.sort(attributesList, new Comparator<AttributeFormat>() {
            public int compare(AttributeFormat format1, AttributeFormat format2) {
                return format1.getOrdinal() - format2.getOrdinal();
            }
        });
    }

    private class AttributeFormat {

        ModelAttribute attribute;
        ModelEntity entity;
        int ordinal;
        int length;
        String formatFunction;

        public AttributeFormat(ModelAttribute attribute, ModelEntity entity) {
            this.attribute = attribute;
            this.entity = entity;
        }

        public String getAttributeId() {
            return attribute.getId();
        }

        public int getOrdinal() {
            return ordinal;
        }

        public void setOrdinal(int ordinal) {
            this.ordinal = ordinal;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public void setFormatFunction(String formatFunction) {
            this.formatFunction = formatFunction;
        }

        public String getFormatFunction() {
            return formatFunction;
        }
        
        public ModelAttribute getAttribute() {
            return attribute;
        }
        
        public ModelEntity getEntity() {
            return entity;
        }
    }

}