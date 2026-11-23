import { useImperativeHandle, forwardRef, useMemo, useRef } from 'react'
import { View, TouchableOpacity } from 'react-native'

import Modal, { type ModalType } from './Modal'
import { Icon } from '@/components/common/Icon'
import { useKeyboard } from '@/utils/hooks'
import { createStyle } from '@/utils/tools'
import { useApple, radius } from '@/theme/apple'
import Text from './Text'
import { scaleSizeH } from '@/utils/pixelRatio'

const HEADER_HEIGHT = 44
const styles = createStyle({
  centeredView: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  modalView: {
    maxWidth: '88%',
    minWidth: '70%',
    maxHeight: '78%',
    // backgroundColor: 'white',
    borderRadius: radius.alert,
    overflow: 'hidden',
    // shadowColor: '#000',
    // shadowOffset: {
    //   width: 0,
    //   height: 2,
    // },
    // shadowOpacity: 0.25,
    // shadowRadius: 4,
    elevation: 0,
  },
  header: {
    flexGrow: 0,
    flexShrink: 0,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    height: HEADER_HEIGHT,
  },
  title: {
    paddingLeft: 40,
    paddingRight: 40,
    textAlign: 'center',
    fontWeight: '600',
    flexShrink: 1,
  },
  closeBtn: {
    position: 'absolute',
    right: 0,
    flexGrow: 0,
    flexShrink: 0,
    height: HEADER_HEIGHT,
    justifyContent: 'center',
    alignItems: 'center',
  },
})

export interface DialogProps {
  onHide?: () => void
  keyHide?: boolean
  bgHide?: boolean
  closeBtn?: boolean
  title?: string
  children: React.ReactNode | React.ReactNode[]
  height?: number | `${number}%`
}

export interface DialogType {
  setVisible: (visible: boolean) => void
}

export default forwardRef<DialogType, DialogProps>(({
  onHide,
  keyHide = true,
  bgHide = true,
  closeBtn = true,
  title = '',
  children,
  height,
}: DialogProps, ref) => {
  const apple = useApple()
  const { keyboardShown, keyboardHeight } = useKeyboard()
  const modalRef = useRef<ModalType>(null)

  useImperativeHandle(ref, () => ({
    setVisible(visible: boolean) {
      modalRef.current?.setVisible(visible)
    },
  }))

  const closeBtnComponent = useMemo(() => {
    return closeBtn
      ? <TouchableOpacity style={{ ...styles.closeBtn, width: scaleSizeH(HEADER_HEIGHT) }} activeOpacity={0.5} onPress={() => modalRef.current?.setVisible(false)}>
          <View style={{ width: 26, height: 26, borderRadius: 13, backgroundColor: apple.fill, alignItems: 'center', justifyContent: 'center' }}>
            <Icon name="close" color={apple.secondaryLabel} size={9} />
          </View>
        </TouchableOpacity>
      : null
  }, [closeBtn, apple])

  return (
    <Modal onHide={onHide} keyHide={keyHide} bgHide={bgHide} bgColor={apple.mask} ref={modalRef}>
      <View style={{ ...styles.centeredView, paddingBottom: keyboardShown ? keyboardHeight : 0 }}>
        <View style={{ ...styles.modalView, height, backgroundColor: apple.alertBackground }} onStartShouldSetResponder={() => true}>
          <View style={styles.header}>
            <Text style={styles.title} size={17} color={apple.label} numberOfLines={1}>{title}</Text>
            {closeBtnComponent}
          </View>
          {children}
        </View>
      </View>
    </Modal>
  )
})
