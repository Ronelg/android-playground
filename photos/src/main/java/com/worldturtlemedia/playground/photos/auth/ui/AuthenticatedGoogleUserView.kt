package com.worldturtlemedia.playground.photos.auth.ui

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import coil.api.load
import coil.transform.CircleCropTransformation
import com.worldturtlemedia.playground.common.base.ui.viewbinding.Binding
import com.worldturtlemedia.playground.common.ktx.onClick
import com.worldturtlemedia.playground.photos.R
import com.worldturtlemedia.playground.photos.databinding.AuthenticatedGoogleUserViewBinding
import com.worldturtlemedia.playground.photos.databinding.AuthenticatedGoogleUserViewBinding.bind

class AuthenticatedGoogleUserView : ConstraintLayout, Binding<AuthenticatedGoogleUserViewBinding> {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    init {
        inflate(context, R.layout.authenticated_google_user_view, this)
    }

    override val binding: AuthenticatedGoogleUserViewBinding by lazy { bind(this) }

    private var onDisconnectClickListener: () -> Unit = {}

    override fun onFinishInflate() {
        super.onFinishInflate()

        binding.btnDisconnect.onClick { onDisconnectClickListener() }
    }

    fun updateInfo(
        email: String,
        avatarUrl: Uri?
    ) = updateInfo(AuthenticatedGoogleUserState(email, avatarUrl))

    fun updateInfo(info: AuthenticatedGoogleUserState) = with(info) {
        binding.txtAuthEmail.text = context.getString(R.string.google_auth_email_display, email)
        binding.imgAvatar.load(avatarUrl) {
            crossfade(true)
            placeholder(R.drawable.ic_person)
            transformations(CircleCropTransformation())
        }
    }

    fun setOnDisconnect(block: () -> Unit) {
        onDisconnectClickListener = block
    }
}

data class AuthenticatedGoogleUserState(
    val email: String,
    val avatarUrl: Uri?
)