package com.alibaba.csp.sentinel.dashboard.rule.apollo;///*
// * Copyright 1999-2018 Alibaba Group Holding Ltd.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.ApiDefinitionEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.GatewayFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.AuthorityRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.SystemRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.dashboard.rule.RuleTypeEnum;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.fastjson.JSON;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.dto.NamespaceReleaseDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author hantianwei@gmail.com
 * @since 1.5.0
 */
@Component("ruleApolloPublisher")
public class RuleApolloPublisher<M> implements DynamicRulePublisher<List<M>> {

    @Value("${spring.profiles.active}")
    private String env;

    @Value("${apollo.cluster.name:default}")
    private String apolloClusterName;

    @Value("${apollo.account:apollo}")
    private String apolloAccount;

    @Value("${apollo.namespace.name:sentinel}")
    private String namespaceName;

    @Autowired
    private ApolloOpenApiClient apolloOpenApiClient;

    @Override
    public void publish(String app, List<M> rules, RuleTypeEnum ruleTypeEnum){
        AssertUtil.notEmpty(app, "app name cannot be empty");
        if (rules == null) {
            return;
        }

        // Increase the configuration
        OpenItemDTO openItemDTO = new OpenItemDTO();
        openItemDTO.setKey(ruleTypeEnum.getName());
        openItemDTO.setValue(convert(rules, ruleTypeEnum));
        openItemDTO.setComment("sentinel auto-join");
        openItemDTO.setDataChangeCreatedBy(apolloAccount);
        apolloOpenApiClient.createOrUpdateItem(app, env, apolloClusterName, namespaceName, openItemDTO);

        // Release configuration
        NamespaceReleaseDTO namespaceReleaseDTO = new NamespaceReleaseDTO();
        namespaceReleaseDTO.setEmergencyPublish(true);
        namespaceReleaseDTO.setReleaseComment("sentinel Modify or add configurations");
        namespaceReleaseDTO.setReleasedBy(apolloAccount);
        namespaceReleaseDTO.setReleaseTitle("sentinel Modify or add configurations");
        apolloOpenApiClient.publishNamespace(app, env, apolloClusterName, namespaceName, namespaceReleaseDTO);
    }

    private String convert(List<M> rules, RuleTypeEnum ruleTypeEnum) {
        String ruleStr = null;
        if (ruleTypeEnum.equals(RuleTypeEnum.FLOW)) {
            List<FlowRuleEntity> rules1 = (List<FlowRuleEntity>) rules;
            ruleStr = JSON.toJSONString(rules1.stream().map(FlowRuleEntity::toRule).collect(Collectors
                    .toList()));
        } else if (ruleTypeEnum.equals(RuleTypeEnum.DEGRADE)) {
            List<DegradeRuleEntity> rules1 = (List<DegradeRuleEntity>) rules;
            ruleStr = JSON.toJSONString(rules1.stream().map(DegradeRuleEntity::toRule).collect(Collectors
                    .toList()));
        } else if (ruleTypeEnum.equals(RuleTypeEnum.PARAM_FLOW)) {
            List<ParamFlowRuleEntity> rules1 = (List<ParamFlowRuleEntity>) rules;
            ruleStr = JSON.toJSONString(rules1.stream().map(ParamFlowRuleEntity::toRule).collect(Collectors
                    .toList()));
        } else if (ruleTypeEnum.equals(RuleTypeEnum.SYSTEM)) {
            List<SystemRuleEntity> rules1 = (List<SystemRuleEntity>) rules;
            ruleStr = JSON.toJSONString(rules1.stream().map(SystemRuleEntity::toRule).collect(Collectors
                    .toList()));
        } else if (ruleTypeEnum.equals(RuleTypeEnum.AUTHORITY)) {
            List<AuthorityRuleEntity> rules1 = (List<AuthorityRuleEntity>) rules;
            ruleStr = JSON.toJSONString(rules1.stream().map(AuthorityRuleEntity::toRule).collect(Collectors
                    .toList()));
        } else if (ruleTypeEnum.equals(RuleTypeEnum.GW_FLOW)) {
            List<GatewayFlowRuleEntity> rules1 = (List<GatewayFlowRuleEntity>) rules;
            ruleStr = JSON.toJSONString(rules1.stream().map(GatewayFlowRuleEntity::toRule).collect(Collectors
                    .toList()));
        } else if (ruleTypeEnum.equals(RuleTypeEnum.GW_API_GROUP)) {
            List<ApiDefinitionEntity> rules1 = (List<ApiDefinitionEntity>) rules;
            ruleStr = JSON.toJSONString(rules1.stream().map(ApiDefinitionEntity::toRule).collect(Collectors
                    .toList()));
        }
        return ruleStr;
    }
}
