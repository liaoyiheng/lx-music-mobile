import { useRef, forwardRef, useImperativeHandle } from 'react'
import { TouchableOpacity } from 'react-native'
import { IonIcon } from '@/components/common/Icon'
import Modal, { type ModalType } from './Modal'
import { type Source } from '@/store/songlist/state'
import { createStyle } from '@/utils/tools'
import { useApple, radius } from '@/theme/apple'
import { navigations } from '@/navigation'
import commonState from '@/store/common/state'

export interface OpenListType {
  setInfo: (source: Source) => void
}

export default forwardRef<OpenListType, {}>((props, ref) => {
  const apple = useApple()
  const modalRef = useRef<ModalType>(null)
  const songlistInfoRef = useRef<{ source: Source }>({ source: 'kw' })

  useImperativeHandle(ref, () => ({
    setInfo(source) {
      songlistInfoRef.current.source = source
    },
  }))

  const handleOpenSonglist = (id: string) => {
    navigations.pushSonglistDetailScreen(commonState.componentIds.home!, {
      play_count: undefined,
      id,
      author: '',
      name: '',
      img: undefined,
      desc: undefined,
      source: songlistInfoRef.current.source,
    })
  }

  return (
    <>
      <TouchableOpacity
        style={{ ...styles.button, backgroundColor: apple.fill }}
        activeOpacity={0.6}
        onPress={() => modalRef.current?.show(songlistInfoRef.current.source)}
      >
        <IonIcon name="link-outline" rawSize={16} color={apple.accent} />
      </TouchableOpacity>
      <Modal ref={modalRef} onOpenId={handleOpenSonglist} />
    </>
  )
})

const styles = createStyle({
  button: {
    width: 30,
    height: 30,
    borderRadius: radius.pill,
    alignItems: 'center',
    justifyContent: 'center',
  },
})
