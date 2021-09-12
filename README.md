# Document-Store
Semester project for Data Structures course, stores documents and allows search and pdf/txt conversion

- Built HashTable and later B-Tree from scratch to store documents in memory and on disk
- Serialized documents for storage as .json files using GSON library
- Implemented PDF <-> TXT conversion using PDFBox library
- Built a priority heap from scratch to determine when memory was exceeded and least recently used files should be written to disk
- Built a Trie from scratch to allow robust search and delete functionality (by word and by prefix)
- Built a Stack from scratch to allow for undo functionality, utilizing lambda expressions
