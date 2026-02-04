# RAG Demo Video Script Outline

## Target Length: 8-12 minutes

---

## 1. HOOK (30 seconds)

**On screen:** You typing a question to ChatGPT about a proprietary document, getting "I don't have access to that information"

**Script:**
> "Large language models are trained on public data—they don't know about YOUR documents. Your company's internal wiki, your research papers, your private data. So how do you get an LLM to answer questions about content it's never seen? That's where RAG comes in."

---

## 2. WHAT IS RAG? (1-2 minutes)

**On screen:** The flow diagram from README or a cleaned-up version

**Script:**
> "RAG stands for Retrieval-Augmented Generation. Instead of training or fine-tuning a model on your data—which is expensive and slow—you retrieve relevant pieces of your documents at query time and include them in the prompt."

**Walk through the flow:**
> "Here's how it works:
> 1. **Load** your documents—PDFs, web pages, whatever
> 2. **Chunk** them into smaller pieces, because we can't fit entire documents in a prompt
> 3. **Embed** each chunk—convert it to a vector that captures its meaning
> 4. **Store** those vectors in a database optimized for similarity search
> 5. When a user asks a question, **Retrieve** the most relevant chunks
> 6. **Generate** an answer by sending the question plus those chunks to the LLM"

> "The LLM sees your data in the prompt context, not in its training. That's the key insight."

---

## 3. THE DEMO PROJECT (30 seconds)

**On screen:** GitHub repo / project structure

**Script:**
> "I've built the same RAG application twice—once in Python using LangChain, and once in Java using Spring AI. Same document, same embedding model, same LLM. Let's see them in action."

> "The document I'm using is the famous 'Attention Is All You Need' paper that introduced the Transformer architecture."

---

## 4. PYTHON DEMO (2-3 minutes)

**On screen:** Terminal running the Python app

**Demo sequence:**
1. Show the command: `python -m ragdemo.main`
2. Point out the startup output (loading, chunking, embedding)
3. Ask: "What is the Transformer architecture?"
4. Show the answer
5. Ask with debug mode: `debug: How does self-attention work?`
6. **Key moment:** Point out the retrieved documents with scores

**Script for debug moment:**
> "Watch what happens when I prefix with 'debug'. Now I can see exactly which chunks the system retrieved. These four pieces of the paper were deemed most relevant to my question. The answer is synthesized from THIS context, not from the model's training data. That's RAG."

---

## 5. JAVA DEMO (1-2 minutes)

**On screen:** Terminal running the Java app

**Demo sequence:**
1. Show the command: `./gradlew bootRun`
2. Same questions, same answers
3. Brief debug mode demo

**Script:**
> "Here's the same thing in Java with Spring AI. Same document, same results. The implementation patterns are different, but the RAG concepts are identical."

> "Why show both? Because in enterprise environments, you'll see RAG implemented in many languages. The framework changes, but the flow—load, chunk, embed, store, retrieve, generate—stays the same."

---

## 6. CODE WALKTHROUGH (2-3 minutes)

**On screen:** Split view of Python and Java files

### Document Loading
**Files:** `document_loader.py` vs `DocumentLoaderService.java`

> "Both versions load PDFs and split them into chunks. Python uses LangChain's RecursiveCharacterTextSplitter—it tries to break on paragraph boundaries. Java uses TokenTextSplitter—it counts tokens. Different strategies, same goal: chunks small enough to fit in a prompt, large enough to contain useful context."

### Vector Store
**Files:** `vector_store.py` vs `RagConfig.java`

> "The vector store is where embeddings live. Both use in-memory stores for simplicity. In production, you'd use something like PostgreSQL with pgvector, Pinecone, or Chroma."

### The RAG Chain
**Files:** `rag_chain.py` vs `RagService.java`

> "Here's where retrieval meets generation. Python uses LangChain's expression language to chain components. Java uses Spring AI's ChatClient with a QuestionAnswerAdvisor that automatically handles retrieval. Different APIs, same pattern."

---

## 7. CUSTOMIZING THE PROMPT (1 minute)

**On screen:** `prompts/rag-prompt.st` file

**Script:**
> "One thing students often overlook: the prompt template matters. Here's what we send to the LLM."

> "We're saying: answer based ONLY on this context. If it's not there, say so. This prevents hallucination—the model won't make things up because we've constrained it."

> "You can customize this. Want bullet points? Change the prompt. Want the model to cite its sources? Change the prompt. RAG gives you control."

---

## 8. BONUS: PERSISTENT STORAGE (1-2 minutes)

**On screen:** Supabase dashboard, schema diagram

**Script:**
> "The main branch uses in-memory storage—fine for demos, but your vectors disappear when the app stops. On a separate branch, I've added Supabase—PostgreSQL with the pgvector extension."

> "Here's something interesting I discovered: even though both apps connect to the same database, they create different tables."

**Show schema diagram:**
> "Spring AI creates one table structure, LangChain creates another. Same database doesn't mean automatic interoperability. If you want true sharing, you need to align on schema—or use the same framework."

> "This is a real-world gotcha worth knowing about."

---

## 9. WHEN TO USE RAG (1 minute)

**Script:**
> "Quick decision guide:
> - Use RAG when your data changes frequently—no retraining needed
> - Use RAG when you need to cite sources—you know exactly what the model saw
> - Use RAG when data is private—it never leaves your infrastructure
> - Consider fine-tuning when you need to change the model's style or behavior fundamentally"

> "For most enterprise use cases, RAG is the right starting point."

---

## 10. WRAP-UP (30 seconds)

**On screen:** GitHub repo link

**Script:**
> "The code is on GitHub—link in the description. Clone it, swap in your own PDFs, and you've got a working RAG system."

> "If you found this useful, subscribe for more AI engineering content. Questions? Drop them in the comments."

---

## B-ROLL / VISUAL SUGGESTIONS

- Terminal output scrolling during document loading
- The "Thinking..." moment before answers appear
- Side-by-side code comparison
- Supabase table view showing vectors
- Diagram animations for the RAG flow

---

## KEY PHRASES TO EMPHASIZE

- "Retrieval, not training"
- "The model sees your data in the prompt, not in its weights"
- "Same concepts, different frameworks"
- "Debug mode shows you what the model actually saw"
