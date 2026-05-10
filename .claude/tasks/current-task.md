# Current Task

Goal:
Implement backup/restore support for Menu Reply module.

Current Scope:

- Menu Reply export/import
- Nested hierarchy restoration
- Parent-child mapping restoration
- Backup manager integration

Requirements:

Backup/Restore:

- Export Menu Reply data
- Restore Menu Reply data
- Preserve nested submenu hierarchy
- Preserve parent-child relationships
- Preserve stop reply configurations
- Reuse Keyword Reply backup architecture

Database:
Include:

- menu replies
- menu items
- hierarchy mappings
- stop reply configurations

Files:

Backup:

- MenuReplyBackupManager.kt

Repository:

- MenuReplyRepository.kt

Database:

- MenuReplyDao.kt

Expected Output:

- Working Menu Reply backup/restore
- Correct hierarchy restoration
- Shared backup architecture reuse

Important:

- Modify only required files
- MVP-only
- Avoid duplicate backup logic