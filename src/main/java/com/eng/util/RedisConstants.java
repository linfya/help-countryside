package com.eng.util;

public class RedisConstants {
    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final Long LOGIN_CODE_TTL = 2L;
    public static final String LOGIN_USER_KEY = "login:token:";
    public static final Long LOGIN_USER_TTL = 36000L;

    public static final String USER_LIKED_BLOG = "user:liked:blog:";
    public static final String USER_COLLECT_BLOG = "user:collect:blog:";
    public static final String USER_COLLECT_ITEM = "user:collect:item:";
    public static final String USER_LIKED_HUMAN = "user:liked:human:";
    public static final String USER_NOTICE_KEY = "user:notice:";





    public static final String BLOG_LIKED_KEY = "blog:liked:";
    public static final String BLOG_COLLECT_KEY = "blog:collect:";
    public static final String HUMAN_LIKED_KEY = "human:liked:";


    public static final String ITEM_COLLECT_KEY = "item:collect:";




    public static final String COMMENTS_LIKES_KEY = "comments:liked:";


    public static final String ORDER_TIMEOUT_QUEUE = "order_queue:timeout:";


    public static final String VIDEO_MESSAGE_KEY = "video:message:";

}
