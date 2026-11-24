import { memo, useState, useMemo, useCallback } from 'react'
import { StyleSheet, View } from 'react-native'
import { IonIcon } from '@/components/common/Icon'
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

/** 顶层评论头像（36×36 圆形） */
const AVATAR_SIZE = 36
/** 回复内头像（圆形，更小） */
const REPLY_AVATAR_SIZE = 24
const AVATAR_GAP = spacing.md
const HORIZONTAL_PAD = spacing.lg

/**
 * 单条评论：36×36 圆形头像 + 昵称 / 时间 / 归属地 + 点赞数 + 15pt 正文 + 图片
 * 回复（楼中楼）放在正文下方的圆角 12 柔和 apple.fill 块中，底部 64dp 左缩进 hairline 分割线
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
  }, [comment.id, comment.reply, apple.fill])

  const likedCount = useMemo(() => {
    if (comment.likedCount == null) return null
    return (
      <View style={styles.like}>
        <IonIcon name="thumbs-up-outline" color={apple.secondaryLabel} rawSize={13} />
        <Text style={styles.likedCount} size={fontSize.caption1} color={apple.secondaryLabel}>{comment.likedCount}</Text>
      </View>
    )
  }, [comment.likedCount, apple.secondaryLabel])

  const showSeparator = !isLast
  const containerStyle = isReply
    ? { ...styles.replyContainer, borderBottomColor: apple.separator, borderBottomWidth: showSeparator ? hairline : 0 }
    : styles.container

  return (
    <View style={containerStyle}>
      <View style={styles.comment}>
        <View style={isReply ? stylesRaw.replyAvatarWrap : stylesRaw.avatarWrap}>
          <Image
            url={comment.avatar && !isAvatarError ? comment.avatar : defaultUser}
            onError={handleAvatarError}
            style={isReply ? stylesRaw.replyAvatar : stylesRaw.avatar}
          />
          <View
            pointerEvents="none"
            style={[
              StyleSheet.absoluteFillObject,
              {
                borderRadius: avatarSize / 2,
                borderWidth: hairline,
                borderColor: apple.coverRing,
              },
            ]}
          />
        </View>
        <View style={{ ...styles.right, paddingLeft: isReply ? spacing.sm : AVATAR_GAP }}>
          <View style={styles.info}>
            <View style={styles.infoLeft}>
              <Text selectable numberOfLines={1} size={isReply ? fontSize.footnote : 14.5} color={apple.label} style={styles.userName}>
                {comment.userName}
              </Text>
              <View style={styles.metaInfo}>
                { comment.timeStr ? <Text numberOfLines={1} size={fontSize.caption1} color={apple.secondaryLabel}>{comment.timeStr}</Text> : null }
                { comment.location ? <Text numberOfLines={1} style={styles.location} size={fontSize.caption1} color={apple.secondaryLabel}>{t('location', { location: comment.location })}</Text> : null }
              </View>
            </View>
            {likedCount}
          </View>
          <CommentText text={comment.text} size={isReply ? 13.5 : fontSize.subhead} />
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
          ? <View style={{ ...styles.separator, backgroundColor: apple.separator }} />
          : null
      }
    </View>
  )
})

const styles = createStyle({
  container: {
    paddingTop: 15,
    paddingHorizontal: HORIZONTAL_PAD,
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
    letterSpacing: -0.15,
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
    gap: 4,
  },
  likedCount: {
    fontWeight: fontWeight.medium,
    fontVariant: ['tabular-nums'],
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
    borderRadius: radius.large,
  },
  separator: {
    height: hairline,
    marginTop: 15,
    marginLeft: AVATAR_SIZE + AVATAR_GAP,
  },
})

const stylesRaw = StyleSheet.create({
  avatarWrap: {
    height: AVATAR_SIZE,
    width: AVATAR_SIZE,
    borderRadius: AVATAR_SIZE / 2,
    overflow: 'hidden',
  },
  avatar: {
    height: AVATAR_SIZE,
    width: AVATAR_SIZE,
    borderRadius: AVATAR_SIZE / 2,
  },
  replyAvatarWrap: {
    height: REPLY_AVATAR_SIZE,
    width: REPLY_AVATAR_SIZE,
    borderRadius: REPLY_AVATAR_SIZE / 2,
    overflow: 'hidden',
  },
  replyAvatar: {
    height: REPLY_AVATAR_SIZE,
    width: REPLY_AVATAR_SIZE,
    borderRadius: REPLY_AVATAR_SIZE / 2,
  },
})

export default CommentFloor
