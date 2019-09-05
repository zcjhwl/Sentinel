package com.alibaba.csp.sentinel.dashboard.rule;

import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.ApiDefinitionEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.GatewayFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.AuthorityRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.SystemRuleEntity;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.system.SystemRule;

/**
 *  
 * @author : zcj
 * @date : Created in 2019/6/14 17:00
 */
public enum  RuleTypeEnum {
    /**
     *
     */
    FLOW("flow.rules", FlowRuleEntity.class),
    DEGRADE("degrade.rules", DegradeRuleEntity.class),
    PARAM_FLOW("param_flow.rules", ParamFlowRule.class),
    SYSTEM("system.rules", SystemRule.class),
    AUTHORITY("authority.rules", AuthorityRule.class),
    GW_FLOW("gw_flow.rules", GatewayFlowRule.class),
    GW_API_GROUP("gw_api_group.rules", ApiDefinition.class)
        ;
    private final String name;

    private final Class proClazz;

    RuleTypeEnum(String name, Class proClazz) {
        this.name = name;
        this.proClazz = proClazz;
    }

    public String getName() {
        return this.name;
    }

    public Class getProClazz() {
        return this.proClazz;
    }
}
