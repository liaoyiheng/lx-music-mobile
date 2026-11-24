import { useEffect, useRef } from 'react'
import { View } from 'react-native'

import MusicList, { type MusicListType } from './MusicList'
import NavBar from './NavBar'
import PageContent from '@/components/PageContent'
import StatusBar from '@/components/common/StatusBar'
import { BottomDockScrim } from '@/components/apple/LiquidGlass'
import { setComponentId } from '@/core/common'
import { COMPONENT_IDS } from '@/config/constant'
import { type ListInfoItem } from '@/store/songlist/state'
import PlayerBar from '@/components/player/PlayerBar'
import { useApple, size } from '@/theme/apple'
import { createStyle } from '@/utils/tools'
import { ListInfoContext } from './state'


export default ({ componentId, info }: { componentId: string, info: ListInfoItem }) => {
  const apple = useApple()
  const musicListRef = useRef<MusicListType>(null)
  const isUnmountedRef = useRef(false)

  useEffect(() => {
    setComponentId(COMPONENT_IDS.songlistDetail, componentId)

    isUnmountedRef.current = false

    musicListRef.current?.loadList(info.source, info.id)


    return () => {
      isUnmountedRef.current = true
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])


  return (
    <PageContent>
      <StatusBar />
      <ListInfoContext.Provider value={info}>
        <View style={{ ...styles.container, backgroundColor: apple.background }}>
          <MusicList ref={musicListRef} componentId={componentId} />
          <NavBar />
          <BottomDockScrim height={96} />
          <View pointerEvents="box-none" style={styles.playerDock}>
            <PlayerBar />
          </View>
        </View>
      </ListInfoContext.Provider>
    </PageContent>
  )
}

const styles = createStyle({
  container: {
    flex: 1,
    position: 'relative',
  },
  playerDock: {
    position: 'absolute',
    left: size.dockMarginX,
    right: size.dockMarginX,
    bottom: size.dockMarginBottom,
    zIndex: 35,
  },
})
