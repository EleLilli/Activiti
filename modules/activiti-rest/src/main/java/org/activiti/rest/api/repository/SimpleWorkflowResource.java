/* Licensed under the Apache License, Version 2.0 (the "License");
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
 */
package org.activiti.rest.api.repository;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversion;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversionFactory;
import org.activiti.workflow.simple.converter.json.SimpleWorkflowJsonConverter;
import org.activiti.workflow.simple.definition.WorkflowDefinition;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;


/**
 * @author Joram Barrez
 */
public class SimpleWorkflowResource extends ServerResource {
  
  @Post
  public SimpleWorkflowSuccessResponse createWorkflow(String json) {
    // Convert json to simple workflow definition
    SimpleWorkflowJsonConverter jsonConverter = new SimpleWorkflowJsonConverter();
    WorkflowDefinition workflowDefinition = jsonConverter.readWorkflowDefinition(json.getBytes());
    
    WorkflowDefinitionConversionFactory conversionFactory = new WorkflowDefinitionConversionFactory();
    WorkflowDefinitionConversion conversion = conversionFactory.createWorkflowDefinitionConversion(workflowDefinition);
    conversion.convert();
    
    // Deploy process
    ProcessEngine processEngine = ActivitiUtil.getProcessEngine();
    RepositoryService repositoryService = processEngine.getRepositoryService();
    BpmnModel bpmnModel = conversion.getBpmnModel();
    Deployment deployment =repositoryService.createDeployment()
      .addBpmnModel(bpmnModel.getProcesses().get(0).getName() + ".bpmn20.xml", bpmnModel)
      .deploy();
    
    // Fetch process definition id
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
            .deploymentId(deployment.getId()).singleResult();
    return new SimpleWorkflowSuccessResponse(processDefinition.getId());
  }
  
  static class SimpleWorkflowSuccessResponse {
    
    protected String processDefinitionId;

    public SimpleWorkflowSuccessResponse(String processDefinitionid) {
      this.processDefinitionId = processDefinitionid;
    }
    
    public String getProcessDefinitionId() {
      return processDefinitionId;
    }

    public void setProcessDefinitionId(String processDefinitionId) {
      this.processDefinitionId = processDefinitionId;
    }
    
  }

}
