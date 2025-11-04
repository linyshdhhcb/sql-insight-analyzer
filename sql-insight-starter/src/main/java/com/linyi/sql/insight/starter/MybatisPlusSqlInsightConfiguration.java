/*
 * Copyright (c) 2025 Lin Yi (linyi)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.linyi.sql.insight.starter;

import com.linyi.sql.insight.core.SqlAnalysisInterceptor;
import com.linyi.sql.insight.mp.MpPluginOrderHelper;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 存在时，确保 SqlAnalysisInterceptor 位于 MP 插件之后。
 *
 * @author linyi
 * @since 1.0.0
 */
@Configuration
@ConditionalOnClass(name = {
        "com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor"
})
@ConditionalOnBean(Interceptor.class)
@AutoConfigureAfter(SqlAnalysisAutoConfiguration.class)
public class MybatisPlusSqlInsightConfiguration {

    /**
     * 创建SQL洞察MyBatis处理器后置处理器Bean
     *
     * @return BeanPostProcessor SQL洞察MyBatis处理器后置处理器实例
     */
    @Bean
    public BeanPostProcessor sqlInsightMpOrderPostProcessor() {
        return new BeanPostProcessor() {
            /**
             * 在Bean初始化完成后进行后置处理
             *
             * @param bean Bean实例
             * @param beanName Bean名称
             * @return 处理后的Bean实例
             * @throws BeansException Bean处理异常
             */
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                // 如果Bean是SqlSessionFactory类型，则处理MyBatis拦截器顺序
                if (bean instanceof SqlSessionFactory) {
                    SqlSessionFactory factory = (SqlSessionFactory) bean;
                    // 遍历MyBatis配置中的所有拦截器
                    for (Interceptor interceptor : factory.getConfiguration().getInterceptors()) {
                        // 如果发现SQL分析拦截器，则确保其在MyBatis Plus插件之后执行
                        if (interceptor instanceof SqlAnalysisInterceptor) {
                            MpPluginOrderHelper.ensureAfterMybatisPlus(factory.getConfiguration(), interceptor);
                            break;
                        }
                    }
                }
                return bean;
            }
        };
    }

}
