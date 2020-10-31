package com.aspire.demo.author;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 这是一个自恋的注解
 *
 * @author {@link tcy}
 * @date 2020/4/24 0:25:59
 */
@SuppressWarnings("all")
@Target(value = {ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface tcy {

    /** 姓名 */
    String[] name() default {"檀朝阳", "tcy"};

    /** 座右铭 */
    String motto() default "我是一只小小小小鸟~嗷！嗷！";

    /** 邮箱 */
    String[] email() default {"164558923@qq.com", "m13685510929@163.com"};

    /** 好好学习，天天向上 */
    String[] DAY_DAY_UP() default {"https://blog.csdn.net/tcy", "https://github.com/tcy?tab=repositories"};
}
