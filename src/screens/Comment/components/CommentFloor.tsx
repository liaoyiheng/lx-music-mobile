import { memo, useState, useMemo, useCallback } from 'react'
import { StyleSheet, View } from 'react-native'
import { Icon } from '@/components/common/Icon'
import { createStyle } from '@/utils/tools'
import { type Comment } from '../utils'
import Text from '@/components/common/Text'
import { useLayout } from '@/utils/hooks'
import { useI18n } from '@/lang'
import Image from '@/components/common/Image'
import CommentImage from './CommentImage'
import CommentText from './CommentText'
import { useApple, fontSize, fontWeight, hairline, radius, spacing } from '@/theme/apple'
// eslint-disable-next-line @typescript-eslint/no-var-requires
const defaultUser = require('@/resources/images/defaultUser.jpg')

/** 顶层评论头像（圆形） */
const AVATAR_SIZE = spacing.xxxl + spacing.xs
/** 回复内头像（圆形，更小） */
const REPLY_AVATAR_SIZE = spacing.xxl
const AVATAR_GAP = spacing.md

/**
 * 单条评论：圆形头像 + 昵称 / 时间 / 归属地 + 点赞数 + 正文 + 图片
 * 回复（楼中楼）放在正文下方的浅色圆角块中，按头像宽度缩进
 */
const CommentFloor = memo(({ comment, isReply, isLast }: {
  comment: Comment
  isReply?: boolean
  isLast?: boolean
}) => {
  const apple = useApple()
  const [isAvatarError, setIsAvatarError] = useState(false)
  const { onLayout, width } = useLayout()
  const t = useI18n()

  const handleAvatarError = useCallback(() => {
    setIsAvatarError(true)
  }, [])

  const avatarSize = isReply ? REPLY_AVATAR_SIZE : AVATAR_SIZE

  const replyComments = useMemo(() => {
    if (!comment.reply?.length) return null
    const endIndex = comment.reply.length - 1
    return (
      <View style={{ ...styles.replyBlock, backgroundColor: apple.fill }}>
        {
          comment.reply.map((c, index) => (
            <CommentFloor comment={c} isReply isLast={index === endIndex} key={`${comment.id}_${c.id}`} />
          ))
        }
      </View>
    )
  }, [comment.id, comment.reply, apple])

  const likedCount = useMemo(() => {
    if (comment.likedCount == null) return null
    return (
      <View style={styles.like}>
        <Icon name="thumbs-up" color={apple.secondaryLabel} size={fontSize.caption1} />
        <Text style={styles.likedCount} size={fontSize.caption1} color={apple.secondaryLabel}>{comment.likedCount}</Text>
      </View>
    )
  }, [comment.likedCount, apple])

  const showSeparator = !isLast
  const containerStyle = isReply
    ? { ...styles.replyContainer, borderBottomColor: apple.separator, borderBottomWidth: showSeparator ? hairline : 0 }
    : styles.container

  return (
    <View style={containerStyle}>
      <View style={styles.comment}>
        <Image
          url={comment.avatar && !isAvatarError ? comment.avatar : defaultUser}
          onError={handleAvatarError}
          style={isReply ? stylesRaw.replyAvatar : stylesRaw.avatar} />
        <View style={{ ...styles.right, paddingLeft: isReply ? spacing.sm : AVATAR_GAP }}>
          <View style={styles.info}>
            <View style={styles.infoLeft}>
              <Text selectable numberOfLines={1} size={isReply ? fontSize.footnote : fontSize.subhead} color={apple.label} style={styles.userName}>
                {comment.userName}
              </Text>
              <View style={styles.metaInfo}>
                { comment.timeStr ? <Text numberOfLines={1} size={fontSize.caption1} color={apple.secondaryLabel}>{comment.timeStr}</Text> : null }
                { comment.location ? <Text numberOfLines={1} style={styles.location} size={fontSize.caption1} color={apple.secondaryLabel}>{t('location', { location: comment.location })}</Text> : null }
              </View>
            </View>
            {likedCount}
          </View>
          <CommentText text={comment.text} size={isReply ? fontSize.subhead : fontSize.callout} />
          {
            comment.images?.length
              ? (
                  <View style={styles.images} onLayout={onLayout}>
                    {
                      comment.images.map((url, index) => <CommentImage key={String(index)} url={url} maxWidth={width} />)
                    }
                  </View>
                )
              : null
          }
        </View>
      </View>
      {
        replyComments
          ? <View style={{ paddingLeft: avatarSize + AVATAR_GAP }}>{replyComments}</View>
          : null
      }
      {
        !isReply && showSeparator
          ? <View style={{ ...styles.separator, marginLeft: AVATAR_SIZE + AVATAR_GAP, backgroundColor: apple.separator }} />
          : null
      }
    </View>
  )
})

const styles = createStyle({
  container: {
    paddingTop: spacing.md,
  },
  replyContainer: {
    paddingVertical: spacing.sm,
  },
  comment: {
    flexDirection: 'row',
  },
  right: {
    flex: 1,
  },
  info: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
  },
  infoLeft: {
    flex: 1,
    marginRight: spacing.sm,
  },
  userName: {
    fontWeight: fontWeight.semibold,
  },
  metaInfo: {
    marginTop: spacing.xxs,
    flexDirection: 'row',
    flexWrap: 'wrap',
  },
  location: {
    marginLeft: spacing.sm,
  },
  like: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingTop: spacing.xxs,
  },
  likedCount: {
    marginLeft: spacing.xs,
  },
  images: {
    paddingTop: spacing.sm,
    width: '100%',
    flexDirection: 'row',
  },
  replyBlock: {
    marginTop: spacing.sm,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.xs,
    borderRadius: radius.medium,
  },
  separator: {
    height: hairline,
    marginTop: spacing.md,
  },
})

const stylesRaw = StyleSheet.create({
  avatar: {
    height: AVATAR_SIZE,
    width: AVATAR_SIZE,
    borderRadius: AVATAR_SIZE / 2,
  },
  replyAvatar: {
    height: REPLY_AVATAR_SIZE,
    width: REPLY_AVATAR_SIZE,
    borderRadius: REPLY_AVATAR_SIZE / 2,
  },
})

export default CommentFloor
