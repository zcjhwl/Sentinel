/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
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
 */
package com.alibaba.csp.sentinel.dashboard.rule.apollo;

import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPathPredicateItem;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.csp.sentinel.dashboard.rule.RuleTypeEnum;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenNamespaceDTO;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * @author hantianwei@gmail.com
 * @since 1.5.0
 */
@Component("ruleApolloProvider")
public class RuleApolloProvider<M> implements DynamicRuleProvider<List<M>> {

    @Value("${spring.profiles.active}")
    private String env;

    @Value("${apollo.cluster.name:default}")
    private String apolloClusterName;

    @Value("${apollo.namespace.name:sentinel}")
    private String namespaceName;

    @Autowired
    private ApolloOpenApiClient apolloOpenApiClient;

    @Override
    public List<M> getRules(String appName, RuleTypeEnum ruleTypeEnum) {
        OpenNamespaceDTO openNamespaceDTO = apolloOpenApiClient
                .getNamespace(appName, env, apolloClusterName, namespaceName);
        String rules = openNamespaceDTO
                .getItems()
                .stream()
                .filter(p -> p.getKey().equals(ruleTypeEnum.getName()))
                .map(OpenItemDTO::getValue)
                .findFirst()
                .orElse("");
        if (StringUtil.isEmpty(rules)) {
            return new ArrayList<>();
        }
        // todo 特殊情况解决
        if (ruleTypeEnum.getProClazz().getSimpleName().equals("ApiDefinition")) {
            List<JSONObject> jsonObjects = JSONObject.parseArray(rules, JSONObject.class);
            if (CollectionUtils.isEmpty(jsonObjects)) {
                return new ArrayList<>();
            } else {
                List<ApiDefinition> result = new ArrayList<>(jsonObjects.size());
                for (JSONObject jsonObject: jsonObjects) {
                    JSONArray predicateItems = jsonObject.getJSONArray("predicateItems");
                    String apiName = jsonObject.getString("apiName");
                    ApiDefinition apiDefinition = new ApiDefinition();
                    apiDefinition.setApiName(apiName);
                    List<ApiPathPredicateItem> apiPathPredicateItems = JSONObject
                            .parseArray(predicateItems.toJSONString(), ApiPathPredicateItem.class);
                    apiDefinition.setPredicateItems(new HashSet<>(apiPathPredicateItems));
                    result.add(apiDefinition);
                }
                return (List<M>) result;
            }
        } else {
            return JSON.parseArray(rules, ruleTypeEnum.getProClazz());
        }
    }
}
