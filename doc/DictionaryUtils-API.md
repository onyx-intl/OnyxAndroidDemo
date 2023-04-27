update `onyx-base-sdk` to version 1.4.3.7 or above


This Method may block thread, Do not invoke in UI thread, for more detail to see [DictionaryActivity](../app/src/main/java/com/android/onyx/demo/DictionaryActivity.java)

```
    /**
     * 
     * @param context  
     * @param keyword
     * @return DictionaryQuery
     *         
     */
    public static DictionaryQuery queryKeyWord(Context context, String keyword)
```

POJO **DictionaryQuery**  FIELD EXPLAIN


| field |  meaning |
|:--|--:|
| state | the total state of query result |
| `List<DictionaryQuery.Dictionary>` | a list contain result from multiple dictionary |
| `DictionaryQuery.Dictionary` | POJO for single result |
| state | state for single result in list |
| dictName | name of dictionary |
| keyword | the keyword to query |
| explanation | explanation of the keyword | 

> be careful. for single result, even state field is success, the explantion field may null

**state** EXPLAIN


| field | meaning |
|:--|--:|
| DICT_STATE_ERROR | query failed, the dictionary not installed or query occur exception |
| DICT_STATE_PARAM_ERROR | query failed, the params incorrect |
| DICT_STATE_QUERY_SUCCESSFUL | query success |
| DICT_STATE_QUERY_FAILED | query failed |
| DICT_STATE_LOADING | is loading |
| DICT_STATE_NO_DATA | query success, but no data get |


