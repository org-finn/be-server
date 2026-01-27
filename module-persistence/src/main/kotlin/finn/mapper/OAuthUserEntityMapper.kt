package finn.mapper

import finn.entity.OAuthUser
import finn.entity.OAuthUserExposed

fun toDomain(oauthUser: OAuthUserExposed): OAuthUser {
    return OAuthUser.create(oauthUser.id.value, oauthUser.provider, oauthUser.providerId, oauthUser.email)
}