package com.swipeit.app.core.permissions

import android.content.IntentSender
import com.swipeit.app.domain.model.DeletionConsent

/** Android system confirmation (delete request or RecoverableSecurityException action). */
class IntentSenderConsent(val intentSender: IntentSender) : DeletionConsent
