import { forwardRef, useImperativeHandle, useRef } from 'react'
import { View, ScrollView } from 'react-native'
import Dialog, { type DialogType } from './Dialog'
import Button from './Button'
import { createStyle } from '@/utils/tools'
import { useI18n } from '@/lang/index'
import { useApple, hairline } from '@/theme/apple'
import Text from './Text'

const styles = createStyle({
  main: {
    flexShrink: 1,
    marginTop: 4,
    marginLeft: 4,
    marginRight: 4,
    marginBottom: 18,
  },
  content: {
    flexGrow: 0,
    paddingLeft: 14,
    paddingRight: 14,
  },
  btns: {
    flexDirection: 'row',
    borderTopWidth: hairline,
  },
  btnsReversedDirection: {
    flexDirection: 'row-reverse',
  },
  btn: {
    flex: 1,
    height: 44,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 10,
  },
  divider: {
    width: hairline,
  },
  cancelText: {
    fontWeight: '400',
  },
  confirmText: {
    fontWeight: '600',
  },
})

export interface ConfirmAlertProps {
  onCancel?: () => void
  onHide?: () => void
  onConfirm?: () => void
  keyHide?: boolean
  bgHide?: boolean
  closeBtn?: boolean
  title?: string
  text?: string
  cancelText?: string
  confirmText?: string
  showConfirm?: boolean
  disabledConfirm?: boolean
  reverseBtn?: boolean
  children?: React.ReactNode | React.ReactNode[]
}

export interface ConfirmAlertType {
  setVisible: (visible: boolean) => void
}

export default forwardRef<ConfirmAlertType, ConfirmAlertProps>(({
  onHide,
  onCancel,
  onConfirm = () => {},
  keyHide,
  bgHide,
  closeBtn,
  title = '',
  text = '',
  cancelText = '',
  confirmText = '',
  showConfirm = true,
  disabledConfirm = false,
  children,
  reverseBtn = false,
}: ConfirmAlertProps, ref) => {
  const apple = useApple()
  const t = useI18n()

  const dialogRef = useRef<DialogType>(null)

  useImperativeHandle(ref, () => ({
    setVisible(visible: boolean) {
      dialogRef.current?.setVisible(visible)
    },
  }))

  const handleCancel = () => {
    onCancel?.()
    dialogRef.current?.setVisible(false)
  }

  return (
    <Dialog onHide={onHide} keyHide={keyHide} bgHide={bgHide} closeBtn={closeBtn} title={title} ref={dialogRef}>
      <View style={styles.main}>
        <ScrollView style={styles.content} keyboardShouldPersistTaps={'always'}>
          {children ?? <Text size={14} color={apple.label} style={{ textAlign: 'center' }}>{text}</Text>}
        </ScrollView>
      </View>
      <View style={{ ...styles.btns, ...(reverseBtn ? styles.btnsReversedDirection : null), borderTopColor: apple.separator }}>
        <Button style={styles.btn} activeOpacity={0.4} onPress={handleCancel}>
          <Text size={17} color={apple.accent} style={styles.cancelText}>{cancelText || t('cancel')}</Text>
        </Button>
        {showConfirm
          ? (
              <>
                <View style={{ ...styles.divider, backgroundColor: apple.separator }} />
                <Button style={styles.btn} activeOpacity={0.4} onPress={onConfirm} disabled={disabledConfirm}>
                  <Text size={17} color={apple.accent} style={styles.confirmText}>{confirmText || t('confirm')}</Text>
                </Button>
              </>
            )
          : null}
      </View>
    </Dialog>
  )
})
