import { useRef } from 'react'
import { TouchableOpacity, View } from 'react-native'

import ListMenu, { type ListMenuType } from './ListMenu'
import ListNameEdit, { type ListNameEditType } from './ListNameEdit'
import List from './List'
import ListImportExport, { type ListImportExportType } from './ListImportExport'
import { handleRemove, handleSync } from './listAction'
import ListMusicSort, { type ListMusicSortType } from './ListMusicSort'
import DuplicateMusic, { type DuplicateMusicType } from './DuplicateMusic'
import LargeTitle from '@/components/apple/LargeTitle'
import { Icon } from '@/components/common/Icon'
import { useI18n } from '@/lang'
import listState from '@/store/list/state'
import { useApple, size } from '@/theme/apple'
import { createStyle } from '@/utils/tools'

export default () => {
  const t = useI18n()
  const apple = useApple()
  const listMenuRef = useRef<ListMenuType>(null)
  const listNameEditRef = useRef<ListNameEditType>(null)
  const listMusicSortRef = useRef<ListMusicSortType>(null)
  const duplicateMusicRef = useRef<DuplicateMusicType>(null)
  const listImportExportRef = useRef<ListImportExportType>(null)

  const createBtn = (
    <TouchableOpacity
      style={styles.btn}
      activeOpacity={0.6}
      onPress={() => listNameEditRef.current?.showCreate(listState.userList.length)}
    >
      <View style={{ ...styles.btnCircle, backgroundColor: apple.fill }}>
        <Icon name="add_folder" size={16} color={apple.accent} />
      </View>
    </TouchableOpacity>
  )

  return (
    <View style={styles.container}>
      <LargeTitle title={t('apple_tab_library')} right={createBtn} />
      <List onShowMenu={(info, position) => listMenuRef.current?.show(info, position)} />
      <ListNameEdit ref={listNameEditRef} />
      <ListMusicSort ref={listMusicSortRef} />
      <DuplicateMusic ref={duplicateMusicRef} />
      <ListImportExport ref={listImportExportRef} />
      <ListMenu
        ref={listMenuRef}
        onNew={index => listNameEditRef.current?.showCreate(index)}
        onRename={info => listNameEditRef.current?.show(info)}
        onSort={info => listMusicSortRef.current?.show(info)}
        onDuplicateMusic={info => duplicateMusicRef.current?.show(info)}
        onImport={(info, position) => listImportExportRef.current?.import(info, position)}
        onExport={(info, position) => listImportExportRef.current?.export(info, position)}
        onRemove={info => { handleRemove(info) }}
        onSync={info => { handleSync(info) }}
        onSelectLocalFile={(info, position) => listImportExportRef.current?.selectFile(info, position)}
      />
    </View>
  )
}

const styles = createStyle({
  container: {
    flex: 1,
  },
  btn: {
    width: size.hitTarget,
    height: size.hitTarget,
    alignItems: 'center',
    justifyContent: 'center',
  },
  btnCircle: {
    width: 32,
    height: 32,
    borderRadius: 16,
    alignItems: 'center',
    justifyContent: 'center',
  },
})
