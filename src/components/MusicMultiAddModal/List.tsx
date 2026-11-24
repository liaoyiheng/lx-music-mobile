import { useState } from 'react'
import { ScrollView, TouchableOpacity, View } from 'react-native'
import { IonIcon } from '@/components/common/Icon'
import Text from '@/components/common/Text'
import { useMyList } from '@/store/list/hook'
import ListItem, { styles as listStyles } from './ListItem'
import CreateUserList from '../MusicAddModal/CreateUserList'
import { useApple, fontWeight, radius } from '@/theme/apple'
import { useI18n } from '@/lang'
import { createStyle } from '@/utils/tools'

const styles = createStyle({
  scroll: {
    flexGrow: 0,
    maxHeight: 360,
  },
  list: {
    paddingHorizontal: 16,
    paddingBottom: 16,
    gap: 8,
  },
  createWrap: {
    position: 'relative',
  },
  createRow: {
    minHeight: 52,
    paddingHorizontal: 12,
    paddingVertical: 8,
    borderRadius: radius.large,
    borderWidth: 1,
    borderStyle: 'dashed',
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
  },
  createText: {
    flex: 1,
    fontWeight: fontWeight.semibold,
  },
})

const EditListItem = () => {
  const [isEdit, setEdit] = useState(false)
  const apple = useApple()
  const t = useI18n()

  return (
    <View style={styles.createWrap}>
      <TouchableOpacity
        activeOpacity={0.65}
        style={[
          styles.createRow,
          {
            backgroundColor: apple.accentFill,
            borderColor: apple.separator,
          },
        ]}
        onPress={() => { setEdit(true) }}
      >
        <View style={[listStyles.iconBox, { backgroundColor: apple.secondaryBackground, borderColor: apple.coverRing }]}>
          <IonIcon name="add" rawSize={20} color={apple.accent} />
        </View>
        <Text
          style={[styles.createText, { opacity: isEdit ? 0 : 1 }]}
          numberOfLines={1}
          size={15}
          color={apple.accent}
        >
          {t('list_create')}
        </Text>
      </TouchableOpacity>
      {
        isEdit
          ? <CreateUserList isEdit={isEdit} onHide={() => { setEdit(false) }} />
          : null
      }
    </View>
  )
}

export default ({ listId, onPress }: {
  listId: string
  onPress: (listInfo: LX.List.MyListInfo) => void
}) => {
  const allList = useMyList().filter(l => l.id != listId)

  return (
    <ScrollView style={styles.scroll} keyboardShouldPersistTaps="always">
      <View style={styles.list} onStartShouldSetResponder={() => true}>
        {allList.map(info => (
          <ListItem key={info.id} listInfo={info} onPress={onPress} />
        ))}
        <EditListItem />
      </View>
    </ScrollView>
  )
}
