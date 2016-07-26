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
package org.jumpmind.metl.ui.views.design;

import static org.apache.commons.lang.StringUtils.isBlank;

import java.util.Collections;
import java.util.List;

import org.jumpmind.metl.core.runtime.component.definition.XMLComponent;
import org.jumpmind.metl.ui.common.ApplicationContext;
import org.jumpmind.metl.ui.common.UiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.server.ClassResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.DragAndDropWrapper.DragStartMode;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class EditFlowPalette extends VerticalLayout {

    private static final long serialVersionUID = 1L;

    final Logger log = LoggerFactory.getLogger(getClass());

    ApplicationContext context;

    EditFlowPanel designFlowLayout;

    VerticalLayout componentLayout;

    String filterText;

    String projectVersionId;

    public EditFlowPalette(EditFlowPanel designFlowLayout, ApplicationContext context, String projectVersionId) {
        this.context = context;
        this.designFlowLayout = designFlowLayout;
        this.projectVersionId = projectVersionId;

        setHeight(100, Unit.PERCENTAGE);
        setWidth(150, Unit.PIXELS);

        setMargin(new MarginInfo(true, false, false, false));

        TextField filterField = new TextField();
        filterField.setInputPrompt("Filter");
        filterField.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
        filterField.setIcon(FontAwesome.SEARCH);
        filterField.setImmediate(true);
        filterField.setTextChangeEventMode(TextChangeEventMode.LAZY);
        filterField.setTextChangeTimeout(200);
        filterField.addTextChangeListener((event) -> {
            filterText = event.getText().toLowerCase();
            populateComponentPalette();
        });
        addComponent(filterField);

        Panel panel = new Panel();
        panel.setSizeFull();
        panel.addStyleName(ValoTheme.PANEL_BORDERLESS);
        panel.addStyleName(ValoTheme.PANEL_SCROLL_INDICATOR);

        componentLayout = new VerticalLayout();
        componentLayout.setMargin(new MarginInfo(true, false, false, false));
        componentLayout.addStyleName("scrollable");
        panel.setContent(componentLayout);

        addComponent(panel);
        setExpandRatio(panel, 1);

        populateComponentPalette();

    }

    protected ClassResource getImageResourceForComponentType(String projectVersionId, XMLComponent componentDefinition) {
        return new ClassResource(componentDefinition.getClass(),
                UiUtils.getImageResourceNameForComponentType(projectVersionId, componentDefinition.getId(), context));
    }

    protected void populateComponentPalette() {
        componentLayout.removeAllComponents();
        List<XMLComponent> componentDefinitions = context.getComponentDefinitionFactory().getDefinitions(projectVersionId);
        Collections.sort(componentDefinitions);
        for (XMLComponent definition : componentDefinitions) {
            if (isBlank(filterText) || definition.getName().toLowerCase().contains(filterText)
                    || definition.getCategory().toLowerCase().contains(filterText)) {
                ClassResource icon = getImageResourceForComponentType(projectVersionId, definition);
                addItemToFlowPanelSection(definition.getName(), definition.getId(), componentLayout, icon, null);
            }
        }
    }

    protected void addItemToFlowPanelSection(String labelName, String componentType, VerticalLayout componentLayout, ClassResource icon,
            String componentId) {

        FlowPaletteItem paletteItem = new FlowPaletteItem(labelName);
        if (componentId != null) {
            paletteItem.setShared(true);
            paletteItem.setComponentId(componentId);
        } else {
            paletteItem.setComponentType(componentType);
            paletteItem.setShared(false);
        }
        paletteItem.setIcon(icon);
        paletteItem.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
        paletteItem.addStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
        paletteItem.addStyleName("leftAligned");
        paletteItem.setWidth(100, Unit.PERCENTAGE);
        DragAndDropWrapper wrapper = new DragAndDropWrapper(paletteItem);
        wrapper.setSizeUndefined();
        wrapper.setDragStartMode(DragStartMode.WRAPPER);
        componentLayout.addComponent(wrapper);
        componentLayout.setComponentAlignment(wrapper, Alignment.TOP_CENTER);

    }

}
