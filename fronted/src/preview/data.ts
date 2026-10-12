export function createPreviewPosts() {
  return [
    { id: 1, title: '灵感信号：发现新的声音', content: '一份来自社区的音乐灵感档案。\n\n在创作、阅读与探索时，让新的声音为你打开另一种想象。\n\n这里展示的是本地示例数据，方便你在没有启动后端时查看首页和详情页的布局。', likeCount: 24 },
    { id: 2, title: '创作工具箱：把想法变成作品', content: '从草图到成品，每一个想法都值得拥有适合的工具。\n\n收集你的常用工具、整理创作流程，并把经验分享给社区。', likeCount: 42 },
    { id: 3, title: '开放知识档案：连接每一次分享', content: '知识在交流中不断生长。\n\n保存那些启发你的文章，记录实践中的发现，让新的连接从一次分享开始。', likeCount: 18 },
  ].map(item => ({
    ...item,
    authorId: 1,
    authorNickname: '编辑部',
    resourceUrl: 'https://monster-siren.hypergryph.com/',
    commentCount: 1,
    favoriteCount: 8,
    createTime: `2026-10-${String(13 - item.id).padStart(2, '0')} 10:00:00`,
    updateTime: `2026-10-${String(13 - item.id).padStart(2, '0')} 10:00:00`,
  }))
}

export function createPreviewComments(postId: number) {
  return [{ id: postId * 10, postId, authorId: 2, authorNickname: '社区成员', parentId: null,
    content: '这是本地示例评论，用来预览评论区。', likeCount: 3, createTime: '2026-10-12 10:30:00', replies: [] }]
}
