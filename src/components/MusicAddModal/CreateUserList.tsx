import { useState, useRef, useEffect } from 'react'
import { View } from 'react-native'
import Input, { type InputType } from '@/components/common/Input'
import { confirmDialog, createStyle } from '@/utils/tools'
import { useI18n } from '@/lang'
import { createUserList } from '@/core/list'
import listState from '@/store/list/state'
import { useApple, radius } from '@/theme/apple'

export default ({ isEdit, onHide }: {
  isEdit: boolean
  onHide: () => void
}) => {
  const [text, setText] = useState('')
  const inputRef = useRef<InputType>(null)
  const apple = useApple()
  const t = useI18n()

  useEffect(() => {
    if (isEdit) {
      setText('')
      requestAnimationFrame(() => {
        inputRef.current?.focus()
      })
    }
  }, [isEdit])

  const handleSubmitEditing = async() => {
    onHide()
    const name = text.trim()
    if (!name.length || (listState.userList.some(l => l.name == name) && !(await confirmDialog({
      message: global.i18n.t('list_duplicate_tip'),
    })))) return
    void createUserList(listState.userList.length, [{ id: `userlist_${Date.now()}`, name, locationUpdateTime: null }])
  }

  return isEdit
    ? (
      <View style={styles.inputContainer}>
        <Input
          placeholder={t('list_create_input_placeholder')}
          placeholderTextColor={apple.tertiaryLabel}
          selectionColor={apple.accent}
          value={text}
          onChangeText={setText}
          ref={inputRef}
          onBlur={handleSubmitEditing}
          onSubmitEditing={handleSubmitEditing}
          style={[
            styles.input,
            {
              backgroundColor: apple.secondaryBackground,
              color: apple.label,
              borderColor: apple.accent,
            },
          ]}
        />
      </View>
      )
    : null
}

const styles = createStyle({
  inputContainer: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
  },
  input: {
    flex: 1,
    fontSize: 15,
    borderRadius: radius.large,
    borderWidth: 1,
    paddingHorizontal: 14,
    textAlign: 'left',
    height: '100%',
  },
})
