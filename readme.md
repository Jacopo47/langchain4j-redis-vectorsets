# Redis Vector Set Semantic Search Example

This is a demonstration application built with **Java** and **LangChain4j** that uses **Redis Stack** (specifically the RedisGears/RediSearch module) as an **Embedding Store** for performing semantic search on user-inputted sentences.

---

## 🚀 What It Does

The application allows a user to:

1.  **Input Identity:** Provide a **name** and **age** at the start.
2.  **Store Data (Default Action):** Enter sentences which are then converted into **vector embeddings** and saved into a Redis Vector Set along with the user's name and age as metadata.
3.  **Semantic Search:** Query the stored sentences based on their **semantic similarity** (meaning) to the input query, returning the most relevant results.
4.  **Metadata Filtering:** Perform semantic searches that are **filtered** by specific name or age.

---

## 💬 Search Commands

The application supports three query commands, all of which use the underlying **semantic search** capability of the `query` function, but with different filtering options:

| Command | Example Usage | Functionality |
| :--- | :--- | :--- |
| **`:q [query text]`** | `:q I like dogs` | Performs a pure semantic search across **all** stored sentences, returning the top 10 most relevant matches. |
| **`:w [query text]`** | `:w I like dogs` $\rightarrow$ (prompts for name) $\rightarrow$ `Alice` | Performs a semantic search **filtered by name**. The application prompts for a name and only searches among sentences where the metadata field `name` is equal to the input name. |
| **`:a [query text]`** | `:a I like dogs` $\rightarrow$ (prompts for age) $\rightarrow$ `30` | Performs a semantic search **filtered by age**. The application prompts for an age and only searches among sentences where the metadata field `age` is equal to the input age. |

### Termination

The program runs until the user sends an **Interrupt Signal** (typically by pressing **$\text{Ctrl}+\text{C}$**), which triggers a graceful shutdown.