import { useRef } from 'react'
import { View } from 'react-native'

import ListMenu, { type ListMenuType } from './ListMenu'
import ListNameEdit, { type ListNameEditType } from './ListNameEdit'
import List from './List'
import ListImportExport, { type ListImportExportType } from './ListImportExport'
import { handleRemove, handleSync } from './listAction'
import ListMusicSort, { type ListMusicSortType } from './ListMusicSort'
import DuplicateMusic, { type DuplicateMusicType } from './DuplicateMusic'
import LargeTitle from '@/components/apple/LargeTitle'
import { GlassCircleButton } from '@/components/apple/LiquidGlass'
import { useI18n } from '@/lang'
import listState from '@/store/list/state'
import { createStyle } from '@/utils/tools'

export default () => {
  const t = useI18n()
  const listMenuRef = useRef<ListMenuType>(null)
  const listNameEditRef = useRef<ListNameEditType>(null)
  const listMusicSortRef = useRef<ListMusicSortType>(null)
  const duplicateMusicRef = useRef<DuplicateMusicType>(null)
  const listImportExportRef = useRef<ListImportExportType>(null)

  const createBtn = (
    <GlassCircleButton
      icon="add"
      size={32}
      iconSize={18}
      onPress={() => listNameEditRef.current?.showCreate(listState.userList.length)}
      accessibilityLabel={t('list_create')}
    />
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
})
