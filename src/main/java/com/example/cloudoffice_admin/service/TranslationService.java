package com.example.cloudoffice_admin.service;

public interface TranslationService {

    /**
     * 翻译文本内容
     * @param text 要翻译的文本
     * @param sourceLanguage 源语言代码
     * @param targetLanguage 目标语言代码
     * @return 翻译后的文本
     */
    String translateText(String text, String sourceLanguage, String targetLanguage);

    /**
     * 检测文本语言
     * @param text 要检测的文本
     * @return 语言代码
     */
    String detectLanguage(String text);

    /**
     * 批量翻译文本
     * @param texts 要翻译的文本数组
     * @param sourceLanguage 源语言代码
     * @param targetLanguage 目标语言代码
     * @return 翻译后的文本数组
     */
    String[] translateBatch(String[] texts, String sourceLanguage, String targetLanguage);
}
