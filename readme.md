# Redis Vector Set Semantic Search Example

This is a demonstration application built with **Java** and **LangChain4j** 
that uses **Redis' vector sets module** as an **Embedding Store** for performing semantic search on user-inputted sentences.

---

## 💬 Search Commands

The application supports three query commands, all of which use the underlying **semantic search** capability of the `query` function, but with different filtering options:

| Command | Example Usage | Functionality |
| :--- | :--- | :--- |
| **`:q [query text]`** | `:q I like dogs` | Performs a pure semantic search across **all** stored sentences, returning the top 10 most relevant matches. |
| **`:w [query text]`** | `:w I like dogs` $\rightarrow$ (prompts for name) $\rightarrow$ `Alice` | Performs a semantic search **filtered by name**. The application prompts for a name and only searches among sentences where the metadata field `name` is equal to the input name. |
| **`:a [query text]`** | `:a I like dogs` $\rightarrow$ (prompts for age) $\rightarrow$ `30` | Performs a semantic search **filtered by age**. The application prompts for an age and only searches among sentences where the metadata field `age` is equal to the input age. |
