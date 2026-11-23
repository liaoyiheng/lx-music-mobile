import { useEffect, useRef } from 'react'
import { View } from 'react-native'
import { createStyle } from '@/utils/tools'

import MusicList, { type MusicListType } from './MusicList'
import BoardsList, { type BoardsListType, type BoardsListProps } from './BoardsList'
import SourceSelector, { type SourceSelectorType, type SourceSelectorProps } from './SourceSelector'
import { getLeaderboardSetting, saveLeaderboardSetting } from '@/utils/data'
import { getBoardsList } from '@/core/leaderboard'
import { handleCollect, handlePlay } from './listAction'
import boardState from '@/store/leaderboard/state'
import { useApple, hairline, spacing } from '@/theme/apple'

/**
 * 排行榜
 * 顶部：榜单胶囊（横向滚动，长按可播放 / 收藏）+ 右侧来源选择
 * 下方：当前榜单歌曲
 */
export default () => {
  const apple = useApple()
  const musicListRef = useRef<MusicListType>(null)
  const boardsListRef = useRef<BoardsListType>(null)
  const sourceSelectorRef = useRef<SourceSelectorType>(null)
  const boundInfo = useRef<{ source: LX.OnlineSource, id: string | null }>({ source: 'kw', id: null })

  const handleBoundChange = (source: LX.OnlineSource, id: string) => {
    musicListRef.current?.loadList(source, id)
    void saveLeaderboardSetting({
      source,
      boardId: id,
    })
  }

  const onBoundChange: BoardsListProps['onBoundChange'] = (id) => {
    boundInfo.current.id = id
    handleBoundChange(boundInfo.current.source, id)
  }
  const onPlay: BoardsListProps['onPlay'] = (id) => {
    boundInfo.current.id = id
    void handlePlay(id, boardState.listDetailInfo.list)
  }
  const onCollect: BoardsListProps['onCollect'] = (id, name) => {
    boundInfo.current.id = id
    void handleCollect(id, name, boundInfo.current.source)
  }
  const onSourceChange: SourceSelectorProps['onSourceChange'] = (source) => {
    boundInfo.current.source = source
    void getBoardsList(source).then(list => {
      const id = list[0].id
      requestAnimationFrame(() => {
        boardsListRef.current?.setList(list, id)
        requestAnimationFrame(() => {
          boundInfo.current.id = id
          handleBoundChange(source, id)
        })
      })
    })
  }

  useEffect(() => {
    void getLeaderboardSetting().then(({ source, boardId }) => {
      boundInfo.current.source = source
      boundInfo.current.id = boardId
      sourceSelectorRef.current?.setSource(source)
      void getBoardsList(source).then(list => {
        boardsListRef.current?.setList(list, boardId)
      })
      musicListRef.current?.loadList(source, boardId)
    })
  }, [])

  return (
    <View style={styles.container}>
      <View style={{ ...styles.header, borderBottomColor: apple.separator }}>
        <View style={styles.boards}>
          <BoardsList
            ref={boardsListRef}
            onBoundChange={onBoundChange}
            onPlay={onPlay}
            onCollect={onCollect}
          />
        </View>
        <SourceSelector ref={sourceSelectorRef} onSourceChange={onSourceChange} />
      </View>
      <MusicList ref={musicListRef} />
    </View>
  )
}

const styles = createStyle({
  container: {
    flex: 1,
    flexDirection: 'column',
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    height: 46,
    paddingBottom: spacing.xs,
    paddingRight: spacing.sm,
    borderBottomWidth: hairline,
    zIndex: 2,
  },
  boards: {
    flex: 1,
    height: '100%',
    justifyContent: 'center',
  },
})
