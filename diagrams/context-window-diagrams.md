# Context Window Diagrams for RAG Video

These Mermaid diagrams illustrate the context window constraint that RAG solves.

---

## 1. The Problem: Documents Don't Fit

This shows a prompt with limited space — the context window can only hold so much.

```mermaid
block-beta
    columns 3

    block:docs:1
        columns 1
        A["📄 Doc 1<br/>50K tokens"]
        B["📄 Doc 2<br/>30K tokens"]
        C["📄 Doc 3<br/>45K tokens"]
        D["📄 Doc 4<br/>20K tokens"]
    end

    arrow1<["❌ Won't fit!"]>(right)

    block:window:1
        columns 1
        W["Context Window<br/>8K tokens<br/>━━━━━━━━━━<br/><br/><br/><br/>"]
    end
```

Alternative simpler version:

```mermaid
flowchart LR
    subgraph docs["Your Documents"]
        D1["Doc 1: 50K tokens"]
        D2["Doc 2: 30K tokens"]
        D3["Doc 3: 45K tokens"]
    end

    docs -->|"Total: 125K tokens"| X{"Context Window<br/>8K tokens"}

    X -->|"❌ OVERFLOW"| fail["Can't process"]

    style docs fill:#e3f2fd,stroke:#1976d2,color:#000
    style X fill:#ffcdd2,stroke:#c62828,color:#000
    style fail fill:#ef5350,stroke:#c62828,color:#fff
```

---

## 2. Context Window Evolution

Shows how context windows have grown over time.

```mermaid
timeline
    title Context Window Sizes Over Time
    2022 : GPT-3 : 4K tokens
    2023 : GPT-4 : 8K → 32K tokens
         : Claude 2 : 100K tokens
    2024 : GPT-4 Turbo : 128K tokens
         : Claude 3 : 200K tokens
    2025 : Gemini 2.5 Pro : 1M tokens
         : Gemini 3 Flash : 1M tokens
         : Gemini 3 Pro : 2M tokens
```

Alternative bar-style visualization:

```mermaid
xychart-beta
    title "Context Window Growth (tokens)"
    x-axis [2022, 2023, 2024, 2025]
    y-axis "Tokens (thousands)" 0 --> 2000
    bar [4, 100, 200, 2000]
```

---

## 3. RAG: The Solution

Shows how RAG retrieves only relevant chunks that fit.

```mermaid
flowchart TB
    subgraph docs["Document Store (any size)"]
        D1["Chunk 1"]
        D2["Chunk 2"]
        D3["Chunk 3"]
        D4["Chunk 4"]
        D5["Chunk 5"]
        D6["...hundreds more..."]
    end

    Q["User Question"] --> E["Embed Query"]
    E --> S["Similarity Search"]
    S --> docs

    D2 -.->|"relevant"| R
    D4 -.->|"relevant"| R

    subgraph R["Retrieved Context"]
        RC1["Chunk 2"]
        RC2["Chunk 4"]
    end

    subgraph window["Context Window ✓"]
        P["Question + Relevant Chunks<br/>━━━━━━━━━━━━━━━━<br/>Fits!"]
    end

    R --> window
    Q --> window
    window --> LLM["LLM"] --> A["Answer"]

    style docs fill:#e3f2fd,stroke:#1976d2,color:#000
    style D2 fill:#c8e6c9,stroke:#388e3c,color:#000
    style D4 fill:#c8e6c9,stroke:#388e3c,color:#000
    style R fill:#a5d6a7,stroke:#388e3c,color:#000
    style window fill:#81c784,stroke:#2e7d32,color:#000
    style Q fill:#e1bee7,stroke:#7b1fa2,color:#000
    style E fill:#ce93d8,stroke:#7b1fa2,color:#000
    style S fill:#ce93d8,stroke:#7b1fa2,color:#000
    style LLM fill:#ffcc80,stroke:#ef6c00,color:#000
    style A fill:#a5d6a7,stroke:#388e3c,color:#000
```

---

## 4. What Are Embeddings?

Shows how similar concepts cluster together in vector space.

**Diagram 4a: Similar concepts cluster together**

```
        EMBEDDING SPACE (2D projection of 384+ dimensions)

                         Dimension 2
                             ↑
                             │
          🏙️ CITIES          │          🍎 FRUITS
                             │
            Minneapolis ●    │        ● pear
                             │
               Detroit ●     │     ● apple
                             │
               Chicago ●     │   ● banana
                             │
                             │       ● orange
        ─────────────────────┼─────────────────────→ Dimension 1
                             │
                             │
          🐕 ANIMALS         │
                             │
                   dog ●     │
                             │
                   cat ●     │
                             │
                rabbit ●     │
                             │

        Key insight: Words with similar meanings
        end up near each other in vector space.
```

**Diagram 4b: How RAG retrieval works**

```
        QUERY: "What fruits are high in vitamin C?"

                         Dimension 2
                             ↑
                             │
          🏙️ CITIES          │          🍎 FRUITS
                             │
            Minneapolis ●    │        ● pear
                             │            ╲
               Detroit ●     │     ● apple ─── ★ QUERY VECTOR
                             │            ╱     "fruits...vitamin C"
               Chicago ●     │   ● banana
                             │
                             │       ● orange ←─── MATCH (0.94)
        ─────────────────────┼─────────────────────→ Dimension 1
                             │
                             │
          🐕 ANIMALS         │
                             │
                   dog ●     │     The query embeds IN the fruit
                             │     cluster because it's semantically
                   cat ●     │     similar. RAG retrieves the
                             │     nearest chunks.
                rabbit ●     │
                             │

        Similarity scores: orange (0.94), apple (0.92), pear (0.89)
        These chunks get sent to the LLM as context.
```

### Key Teaching Points for Embeddings

When showing this diagram, emphasize:

1. **Similar meanings → nearby vectors**
   - "apple" and "banana" are close because they're both fruits
   - "Chicago" and "Detroit" are close because they're both Midwest cities

2. **The actual dimensions are abstract**
   - Real embeddings have 384, 768, or 1536 dimensions
   - We project down to 2D just to visualize

3. **Distance = semantic similarity**
   - RAG finds chunks whose vectors are closest to your question's vector
   - That's how it knows which text is "relevant"

---

## Usage Notes

These diagrams can be rendered:
- **Mermaid Live Editor**: https://mermaid.live
- **VS Code**: With Mermaid preview extension
- **GitHub**: Renders Mermaid in markdown automatically
- **Export**: Render to PNG/SVG for video overlay

For the video:
1. Diagram #1 — "The Problem" (documents don't fit)
2. Diagram #2 — "Context windows are growing" (timeline)
3. Diagram #3 — "How RAG solves this" (retrieval flow)
4. Diagram #4a — "What embeddings are" (clustering)
5. Diagram #4b — "How retrieval works" (query vector)
