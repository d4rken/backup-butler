package eu.darken.bb.common.user

import eu.darken.bb.common.dagger.PerApp
import javax.inject.Inject

@PerApp
class UserManagerBB @Inject constructor() {

    val currentUser: UserHandleBB
        get() {
            return UserHandleBB(userId = 0)
        }

}