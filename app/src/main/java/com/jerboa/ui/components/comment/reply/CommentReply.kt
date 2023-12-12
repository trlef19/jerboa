
package com.jerboa.ui.components.comment.reply

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import com.jerboa.R
import com.jerboa.datatypes.sampleCommentView
import com.jerboa.db.entity.Account
import com.jerboa.ui.components.comment.CommentNodeHeader
import com.jerboa.ui.components.comment.mentionnode.CommentMentionNodeHeader
import com.jerboa.ui.components.comment.replynode.CommentReplyNodeHeader
import com.jerboa.ui.components.common.MarkdownTextField
import com.jerboa.ui.components.post.PostNodeHeader
import com.jerboa.ui.theme.LARGE_PADDING
import com.jerboa.ui.theme.MEDIUM_PADDING
import it.vercruysse.lemmyapi.v0x19.datatypes.CommentReplyView
import it.vercruysse.lemmyapi.v0x19.datatypes.CommentView
import it.vercruysse.lemmyapi.v0x19.datatypes.PersonMentionView
import it.vercruysse.lemmyapi.v0x19.datatypes.PostView

@Composable
fun RepliedComment(
    commentView: CommentView,
    onPersonClick: (personId: Int) -> Unit,
    showAvatar: Boolean,
    showScores: Boolean,
) {
    Column(modifier = Modifier.padding(MEDIUM_PADDING)) {
        CommentNodeHeader(
            commentView = commentView,
            onPersonClick = onPersonClick,
            score = commentView.counts.score,
            myVote = commentView.my_vote,
            collapsedCommentsCount = 0,
            isExpanded = true,
            onClick = {},
            onLongClick = {},
            showAvatar = showAvatar,
            showScores = showScores,
        )
        SelectionContainer {
            Text(text = commentView.comment.content)
        }
    }
}

@Composable
fun RepliedCommentReply(
    commentReplyView: CommentReplyView,
    onPersonClick: (personId: Int) -> Unit,
    showAvatar: Boolean,
    showScores: Boolean,
) {
    Column(modifier = Modifier.padding(MEDIUM_PADDING)) {
        CommentReplyNodeHeader(
            commentReplyView = commentReplyView,
            onPersonClick = onPersonClick,
            score = commentReplyView.counts.score,
            myVote = commentReplyView.my_vote,
            onClick = {},
            onLongClick = {},
            showAvatar = showAvatar,
            showScores = showScores,
        )
        SelectionContainer {
            Text(text = commentReplyView.comment.content)
        }
    }
}

@Composable
fun RepliedMentionReply(
    personMentionView: PersonMentionView,
    onPersonClick: (personId: Int) -> Unit,
    showAvatar: Boolean,
    showScores: Boolean,
) {
    Column(modifier = Modifier.padding(MEDIUM_PADDING)) {
        CommentMentionNodeHeader(
            personMentionView = personMentionView,
            onPersonClick = onPersonClick,
            score = personMentionView.counts.score,
            myVote = personMentionView.my_vote,
            onClick = {},
            onLongClick = {},
            showAvatar = showAvatar,
            showScores = showScores,
        )
        SelectionContainer {
            Text(text = personMentionView.comment.content)
        }
    }
}

@Preview
@Composable
fun RepliedCommentPreview() {
    RepliedComment(
        commentView = sampleCommentView,
        onPersonClick = {},
        showAvatar = true,
        showScores = true,
    )
}

@Composable
fun RepliedPost(
    postView: PostView,
    onPersonClick: (personId: Int) -> Unit,
    showAvatar: Boolean,
    showScores: Boolean,
) {
    Column(modifier = Modifier.padding(MEDIUM_PADDING)) {
        PostNodeHeader(
            postView = postView,
            myVote = postView.my_vote,
            score = postView.counts.score,
            onPersonClick = onPersonClick,
            showAvatar = showAvatar,
            showScores = showScores,
        )
        val text = postView.post.body ?: run { postView.post.name }
        SelectionContainer {
            Text(text = text)
        }
    }
}

@Composable
fun CommentReply(
    commentView: CommentView,
    reply: TextFieldValue,
    onReplyChange: (TextFieldValue) -> Unit,
    onPersonClick: (personId: Int) -> Unit,
    account: Account,
    modifier: Modifier = Modifier,
    showAvatar: Boolean,
    showScores: Boolean,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier.verticalScroll(scrollState),
    ) {
        RepliedComment(
            commentView = commentView,
            onPersonClick = onPersonClick,
            showAvatar = showAvatar,
            showScores = showScores,
        )
        Divider(modifier = Modifier.padding(vertical = LARGE_PADDING))
        MarkdownTextField(
            text = reply,
            onTextChange = onReplyChange,
            account = account,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun CommentReplyReply(
    commentReplyView: CommentReplyView,
    reply: TextFieldValue,
    onReplyChange: (TextFieldValue) -> Unit,
    onPersonClick: (personId: Int) -> Unit,
    account: Account,
    modifier: Modifier = Modifier,
    showAvatar: Boolean,
    showScores: Boolean,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier.verticalScroll(scrollState),
    ) {
        RepliedCommentReply(
            commentReplyView = commentReplyView,
            onPersonClick = onPersonClick,
            showAvatar = showAvatar,
            showScores = showScores,
        )
        Divider(modifier = Modifier.padding(vertical = LARGE_PADDING))
        MarkdownTextField(
            text = reply,
            onTextChange = onReplyChange,
            account = account,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun MentionReply(
    personMentionView: PersonMentionView,
    reply: TextFieldValue,
    onReplyChange: (TextFieldValue) -> Unit,
    onPersonClick: (personId: Int) -> Unit,
    account: Account,
    modifier: Modifier = Modifier,
    showAvatar: Boolean,
    showScores: Boolean,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier.verticalScroll(scrollState),
    ) {
        RepliedMentionReply(
            personMentionView = personMentionView,
            onPersonClick = onPersonClick,
            showAvatar = showAvatar,
            showScores = showScores,
        )
        Divider(modifier = Modifier.padding(vertical = LARGE_PADDING))
        MarkdownTextField(
            text = reply,
            onTextChange = onReplyChange,
            account = account,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun PostReply(
    postView: PostView,
    reply: TextFieldValue,
    onReplyChange: (TextFieldValue) -> Unit,
    onPersonClick: (personId: Int) -> Unit,
    account: Account,
    modifier: Modifier = Modifier,
    showAvatar: Boolean,
    showScores: Boolean,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier.verticalScroll(scrollState),
    ) {
        RepliedPost(
            postView = postView,
            onPersonClick = onPersonClick,
            showAvatar = showAvatar,
            showScores = showScores,
        )
        Divider(modifier = Modifier.padding(vertical = LARGE_PADDING))
        MarkdownTextField(
            text = reply,
            onTextChange = onReplyChange,
            account = account,
            modifier = Modifier.fillMaxWidth(),
            placeholder = stringResource(R.string.comment_reply_type_your_comment),
        )
    }
}
