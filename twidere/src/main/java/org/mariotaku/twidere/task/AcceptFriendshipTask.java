package org.mariotaku.twidere.task;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.mariotaku.microblog.library.MicroBlog;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.User;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.annotation.AccountType;
import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.message.FriendshipTaskEvent;
import org.mariotaku.twidere.model.util.ParcelableAccountUtils;
import org.mariotaku.twidere.util.Utils;

/**
 * Created by mariotaku on 16/3/11.
 */
public class AcceptFriendshipTask extends AbsFriendshipOperationTask implements Constants {

    public AcceptFriendshipTask(final Context context) {
        super(context, FriendshipTaskEvent.Action.ACCEPT);
    }

    @NonNull
    @Override
    protected User perform(@NonNull MicroBlog twitter, @NonNull ParcelableCredentials credentials, @NonNull Arguments args) throws MicroBlogException {
        switch (ParcelableAccountUtils.getAccountType(credentials)) {
            case AccountType.FANFOU: {
                return twitter.acceptFanfouFriendship(args.userKey.getId());
            }
        }
        return twitter.acceptFriendship(args.userKey.getId());
    }

    @Override
    protected void succeededWorker(@NonNull MicroBlog twitter, @NonNull ParcelableCredentials credentials, @NonNull Arguments args, @NonNull ParcelableUser user) {
        Utils.setLastSeen(context, user.key, System.currentTimeMillis());
    }

    @Override
    protected void showErrorMessage(@NonNull Arguments params, @Nullable Exception exception) {
        Utils.showErrorMessage(context, R.string.action_accepting_follow_request, exception, false);
    }

    @Override
    protected void showSucceededMessage(@NonNull Arguments params, @NonNull ParcelableUser user) {
        final boolean nameFirst = preferences.getBoolean(KEY_NAME_FIRST);
        final String message = context.getString(R.string.accepted_users_follow_request,
                manager.getDisplayName(user, nameFirst));
        Utils.showOkMessage(context, message, false);
    }

}
