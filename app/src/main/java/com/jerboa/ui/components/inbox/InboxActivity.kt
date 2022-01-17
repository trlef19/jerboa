package com.jerboa.ui.components.inbox

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import com.jerboa.*
import com.jerboa.db.Account
import com.jerboa.db.AccountViewModel
import com.jerboa.ui.components.comment.CommentNode
import com.jerboa.ui.components.community.CommunityViewModel
import com.jerboa.ui.components.community.communityClickWrapper
import com.jerboa.ui.components.home.BottomAppBarAll
import com.jerboa.ui.components.home.HomeViewModel
import com.jerboa.ui.components.person.PersonProfileViewModel
import com.jerboa.ui.components.person.personClickWrapper
import com.jerboa.ui.components.post.InboxViewModel
import com.jerboa.ui.components.post.PostViewModel
import com.jerboa.ui.components.post.postClickWrapper
import com.jerboa.ui.components.private_message.PrivateMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun InboxActivity(
    navController: NavController,
    inboxViewModel: InboxViewModel,
    homeViewModel: HomeViewModel,
    personProfileViewModel: PersonProfileViewModel,
    postViewModel: PostViewModel,
    communityViewModel: CommunityViewModel,
    accountViewModel: AccountViewModel,
) {

    Log.d("jerboa", "got to inbox activity")

    val scope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()
    val ctx = LocalContext.current
    val accounts by accountViewModel.allAccounts.observeAsState()
    val account = getCurrentAccount(accounts = accounts)
    val unreadCount = homeViewModel.unreadCountResponse?.let { unreadCountTotal(it) }

    Surface(color = MaterialTheme.colors.background) {
        Scaffold(
            scaffoldState = scaffoldState,
            topBar = {
                Column {
                    InboxHeader(
                        unreadCount = unreadCount,
                        navController = navController,
                        selectedUnreadOrAll = unreadOrAllFromBool(inboxViewModel.unreadOnly.value),
                        onClickUnreadOrAll = { unreadOrAll ->
                            inboxViewModel.fetchReplies(
                                account = account,
                                clear = true,
                                changeUnreadOnly = unreadOrAll == UnreadOrAll.Unread,
                                ctx = ctx,
                            )
                            inboxViewModel.fetchPersonMentions(
                                account = account,
                                clear = true,
                                changeUnreadOnly = unreadOrAll == UnreadOrAll.Unread,
                                ctx = ctx,
                            )
                            inboxViewModel.fetchPrivateMessages(
                                account = account,
                                clear = true,
                                changeUnreadOnly = unreadOrAll == UnreadOrAll.Unread,
                                ctx = ctx,
                            )
                        },
                        onClickMarkAllAsRead = {
                            inboxViewModel.markAllAsRead(
                                account = account,
                                ctx = ctx,
                            )
                            homeViewModel.markAllAsRead()
                        }
                    )
                    if (inboxViewModel.loading.value) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            },
            content = {
                InboxTabs(
                    navController = navController,
                    personProfileViewModel = personProfileViewModel,
                    inboxViewModel = inboxViewModel,
                    postViewModel = postViewModel,
                    communityViewModel = communityViewModel,
                    homeViewModel = homeViewModel,
                    ctx = ctx,
                    account = account,
                    scope = scope,
                )
            },
            bottomBar = {
                BottomAppBarAll(
                    unreadCounts = homeViewModel.unreadCountResponse,
                    onClickProfile = {
                        account?.id?.also {
                            personClickWrapper(
                                personProfileViewModel = personProfileViewModel,
                                personId = it,
                                account = account,
                                navController = navController,
                                ctx = ctx,
                            )
                        }
                    },
                    onClickInbox = {
                        inboxClickWrapper(inboxViewModel, account, navController, ctx)
                    },
                    navController = navController,
                )
            }
        )
    }
}

enum class InboxTab {
    Replies,
//    Mentions,
    Messages,
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun InboxTabs(
    navController: NavController,
    personProfileViewModel: PersonProfileViewModel,
    inboxViewModel: InboxViewModel,
    communityViewModel: CommunityViewModel,
    homeViewModel: HomeViewModel,
    ctx: Context,
    account: Account?,
    scope: CoroutineScope,
    postViewModel: PostViewModel,
) {
    val tabTitles = InboxTab.values().map { it.toString() }

    val pagerState = rememberPagerState()

    Column {
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            indicator = { tabPositions -> // 3.
                TabRowDefaults.Indicator(
                    Modifier.pagerTabIndicatorOffset(
                        pagerState,
                        tabPositions
                    )
                )
            }
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = { Text(text = title) }
                )
            }
        }
        HorizontalPager(
            count = tabTitles.size,
            state = pagerState,
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxSize()
        ) { tabIndex ->
            when (tabIndex) {
                InboxTab.Replies.ordinal -> {

                    val nodes = commentsToFlatNodes(inboxViewModel.replies)
                    LazyColumn {
                        items(nodes) { node ->
                            CommentNode(
                                node = node,
                                onUpvoteClick = { commentView ->
                                    inboxViewModel.likeComment(
                                        commentView = commentView,
                                        voteType = VoteType.Upvote,
                                        account = account,
                                        ctx = ctx,
                                    )
                                },
                                onDownvoteClick = { commentView ->
                                    inboxViewModel.likeComment(
                                        commentView = commentView,
                                        voteType = VoteType.Downvote,
                                        account = account,
                                        ctx = ctx,
                                    )
                                },
                                onReplyClick = { commentView ->
                                    // TODO To do replies from elsewhere than postView,
                                    // you need to refetch that post view
                                    postViewModel.replyToCommentParent = commentView
                                    postViewModel.fetchPost(
                                        id = commentView.post.id,
                                        account = account,
                                        ctx = ctx,
                                    )
                                    navController.navigate("commentReply")
                                },
                                onSaveClick = { commentView ->
                                    inboxViewModel.saveComment(
                                        commentView = commentView,
                                        account = account,
                                        ctx = ctx,
                                    )
                                },
                                onMarkAsReadClick = { commentView ->
                                    inboxViewModel.markReplyAsRead(
                                        commentView = commentView,
                                        account = account,
                                        ctx = ctx,
                                    )
                                    homeViewModel.updateUnreads(commentView)
                                },
                                onPersonClick = { personId ->
                                    personClickWrapper(
                                        personProfileViewModel,
                                        personId,
                                        account,
                                        navController,
                                        ctx
                                    )
                                },
                                onCommunityClick = { community ->
                                    communityClickWrapper(
                                        communityViewModel = communityViewModel,
                                        communityId = community.id,
                                        account = account,
                                        navController = navController,
                                        ctx = ctx,
                                    )
                                },
                                onPostClick = { postId ->
                                    postClickWrapper(
                                        postViewModel = postViewModel,
                                        postId = postId,
                                        account = account,
                                        navController = navController,
                                        ctx = ctx,
                                    )
                                },
                                showPostAndCommunityContext = true,
                                showRead = true,
                            )
                        }
                    }
                }

//                InboxTab.Mentions.ordinal -> {
//                    // TODO Need to do a whole type of its own here
//                }
                InboxTab.Messages.ordinal -> {
                    account?.let { acct ->
                        LazyColumn {
                            items(inboxViewModel.messages) { message ->
                                PrivateMessage(
                                    myPersonId = acct.id,
                                    privateMessageView = message,
                                    onReplyClick = { privateMessageView ->
                                        inboxViewModel.replyToPrivateMessageView = privateMessageView
                                        navController.navigate("privateMessageReply")
                                    },
                                    onMarkAsReadClick = { privateMessageView ->
                                        inboxViewModel.markPrivateMessageAsRead(
                                            privateMessageView = privateMessageView,
                                            account = account,
                                            ctx = ctx,
                                        )
                                        homeViewModel.updateUnreads(privateMessageView)
                                    },
                                    onPersonClick = { personId ->
                                        personClickWrapper(
                                            personProfileViewModel,
                                            personId,
                                            account,
                                            navController,
                                            ctx
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}