package com.monglife.mongs.core.kafka.event.enums;

public class EventTopic {

    public static final String COMMIT_PREFIX  = "commit";
    public static final String ROLLBACK_REFIX = "rollback";

    public static final String NOTIFICATION                                     = "notification.mongs";

    public static final String COMMIT_EXCHANGE_CURRENT_WALKING_COUNT            = COMMIT_PREFIX  + ".exchange-current-walking-count";       //      user -> character (with rollback)
    public static final String COMMIT_EXCHANGE_STAR_POINT                       = COMMIT_PREFIX  + ".exchange-star-point";                  //      user -> character (with rollback)
    public static final String COMMIT_EGG_EVOLUTION                             = COMMIT_PREFIX  + ".egg-evolution-mong";                   // character -> character (without rollback)
    public static final String COMMIT_INCREASE_STATUS                           = COMMIT_PREFIX  + ".increase-status";                      // character -> character (without rollback)
    public static final String COMMIT_DECREASE_STATUS                           = COMMIT_PREFIX  + ".decrease-status";                      // character -> character (without rollback)
    public static final String COMMIT_INCREASE_POOP                             = COMMIT_PREFIX  + ".increase-poop";                        // character -> character (without rollback)
    public static final String COMMIT_DEAD                                      = COMMIT_PREFIX  + ".dead";                                 // character -> character (without rollback)
    public static final String COMMIT_SLEEP                                     = COMMIT_PREFIX  + ".sleep";                                // character -> character (without rollback)
    public static final String COMMIT_WAKEUP                                    = COMMIT_PREFIX  + ".wakeup";                               // character -> character (without rollback)
    public static final String COMMIT_CREATE_MONG                               = COMMIT_PREFIX  + ".create-mong";                          // character -> user      (without rollback)
    public static final String COMMIT_EVOLUTION_MONG                            = COMMIT_PREFIX  + ".evolution-mong";                       // character -> user      (without rollback)
    public static final String COMMIT_RANDOM_DRAW_MAP                           = COMMIT_PREFIX  + ".random-draw-map";                      // character -> user      (without rollback)
    public static final String COMMIT_MISSION_REWARD_STAR_POINT                 = COMMIT_PREFIX  + ".mission-reward-star-point";            // character -> user      (without rollback)

    public static final String ROLLBACK_EXCHANGE_CURRENT_WALKING_COUNT          = ROLLBACK_REFIX + ".exchange-current-walking-count";       // character -> user
    public static final String ROLLBACK_EXCHANGE_STAR_POINT                     = ROLLBACK_REFIX + ".exchange-star-point";                  // character -> user
}
