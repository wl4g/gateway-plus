package com.wl4g.gateway.security.ram;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// TODO: testing
public class PolicyToSqlConverterTest {

    @Test
    public void testPolicyToSqlConversionInBizService() {
        // 1. 创建策略转换器实例
        RamRequestContext.SqlCondition condition = getSqlCondition(
                "{" +
                        "    \"Statement\": [{" +
                        "        \"Effect\": \"Allow\"," +
                        "        \"Action\": [\"workflow:run\"]," +
                        "        \"Resource\": \"urn:std:workflow-service:us-west:account101:job/123\"," +
                        "        \"Condition\": {" +
                        "            \"StringEquals\": {" +
                        "                \"$tag:owner\": \"${user.uid}\"," +
                        "                \"$tag:env\": \"production\"" +
                        "            }" +
                        "        }" +
                        "    }]" +
                        "}"
        );

        // 5. 验证SQL条件和参数
        assertEquals("resource_id LIKE ? AND resource_tags @> ?::jsonb", condition.getClause());
        assertEquals(1, condition.getParameters().size());
        assertEquals("123", condition.getParameters().get(0)); // resource_id pattern
        assertEquals("{\"owner\":\"user123\",\"env\":\"production\"}", condition.getParameters().get(0)); // tags

        // 6. 在实际业务中使用生成的SQL (示例)
        String sql = "SELECT * FROM workflow_jobs WHERE " + condition.getClause();
        System.out.println("Generated SQL: " + sql);
        System.out.println("Parameters: " + condition.getParameters());

        // 7. 可以用于JdbcTemplate或MyBatis等 (业务服务端实际使用示例)
        /*
        List<WorkflowJob> jobs = jdbcTemplate.query(
            sql,
            new Object[]{condition.getParameters().get(0), condition.getParameters().get(1)},
            (rs, rowNum) -> {
                WorkflowJob job = new WorkflowJob();
                job.setId(rs.getString("id"));
                job.setName(rs.getString("name"));
                job.setStatus(rs.getString("status"));
                return job;
            }
        );
        */
    }

    private static RamRequestContext.SqlCondition getSqlCondition(String policyJson) {
        PolicyToSqlConverter converter = new PolicyToSqlConverter();

        // 1. 准备用户上下文
        RamRequestContext.UserContext user = new RamRequestContext.UserContext() {
            @Override
            public String getUid() {
                return "user123";
            }

            @Override
            public String getGid() {
                return "group456";
            }
        };

        // 2. 转换策略为SQL条件 (业务服务端调用点)
        return converter.convertToSql(policyJson, user);
    }

    @Test
    public void testResourceArrayPolicyInBizService() {
        RamRequestContext.SqlCondition condition = getSqlCondition(
                "{" +
                        "    \"Statement\": [{" +
                        "        \"Effect\": \"Allow\"," +
                        "        \"Action\": [\"api:invoke\"]," +
                        "        \"Resource\": [" +
                        "            \"urn:std:api-service:us-west:account101:payment-api:order/charge/202501010001\"," +
                        "            \"urn:std:api-service:us-west:account101:payment-api:order/refund/202501010002\"" +
                        "        ]" +
                        "    }]" +
                        "}"
        );

        // 会取第一个资源进行转换
        assertEquals("resource_id LIKE ?", condition.getClause());
        assertEquals("/v1/charge", condition.getParameters().get(0));

        // 业务服务端实际使用示例
        /*
        List<ApiLog> logs = myBatisTemplate.selectList(
            "ApiLogMapper.selectByResource", 
            Map.of("resourcePattern", condition.getParameters().get(0))
        );
        */
    }
}