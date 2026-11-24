import { useRef, forwardRef, useImperativeHandle } from 'react'
import { GlassCircleButton } from '@/components/apple/LiquidGlass'
import Modal, { type ModalType } from './Modal'
import { type Source } from '@/store/songlist/state'
import { navigations } from '@/navigation'
import commonState from '@/store/common/state'

export interface OpenListType {
  setInfo: (source: Source) => void
}

export default forwardRef<OpenListType, {}>((props, ref) => {
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
      <GlassCircleButton
        icon="link-outline"
        iconSize={16}
        size={32}
        onPress={() => modalRef.current?.show(songlistInfoRef.current.source)}
      />
      <Modal ref={modalRef} onOpenId={handleOpenSonglist} />
    </>
  )
})
