package com.anwen.mongo.logic;

import com.anwen.mongo.annotation.logice.IgnoreLogic;
import com.anwen.mongo.manager.LogicManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;

/**
 * 忽略逻辑删除
 *
 * @author loser
 */
@Aspect
@Order(0)
public class MongoLogicIgnoreAspect {

    @Pointcut("@annotation(com.anwen.mongo.annotation.logice.IgnoreLogic)")
    private void markIgnoreLogic() {
    }

    @Around(value = "markIgnoreLogic() && @annotation(ignoreLogic)")
    public Object ignoreLogic(ProceedingJoinPoint joinPoint, IgnoreLogic ignoreLogic) throws Throwable {

        try {
            LogicManager.ignoreLogicCondition();
            return joinPoint.proceed();
        } finally {
            LogicManager.restoreLogicCondition();
        }

    }


}
