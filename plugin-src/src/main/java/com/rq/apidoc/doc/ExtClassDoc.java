package com.rq.apidoc.doc;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.*;
import com.sun.javadoc.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;

public class ExtClassDoc {
	public static final String NEW_LINE = System.getProperty("line.separator");
	static enum Type{
		CLASS,METHOD,FIELD,NONE(0),ALL((~0));
		static {
			// 修改 ALL 的mask 值
			int mask = 0;
			for(Type v : Type.values()){
				if(1 == Integer.bitCount(v.mask))mask |=v.mask;
			}
			try {
				Field field = Type.class.getDeclaredField("mask");
				field.setAccessible(true);
				field.set(ALL, mask);
			} catch (Exception e) {
				throw new ExceptionInInitializerError(e);
			}
			// 检查所有组合mask值的合法性,超出范围则抛出异常
			for(Type v : Type.values()){
				if( Integer.bitCount(v.mask) > 1 && v != ALL){
					if(((~ALL.mask)&v.mask) != 0)
						throw new ExceptionInInitializerError(String.format("%s: %s,invalid mask, out of ALL scope",v.name(),
								Integer.toBinaryString(v.mask)));
				}
			}
		}
		private final int mask;
		Type(){
			this.mask=(1<<this.ordinal());
		}

		Type(int mask){
			Preconditions.checkArgument(1 != Integer.bitCount(mask), "bit count of mask must be not be 1");
			this.mask=mask;
		}
		public boolean check(Integer v){
			return null == v? false :mask == (mask & v);
		}
		public boolean check(Type v){
			return null == v ? false: check(v.mask);
		}
		public int or(int v){
			return mask | v;
		}
		public int reset(int v){
			return (~mask) & v;
		}
		public static int sum(Type ...values){
			if(null==values)return 0;
			int m=0;
			for(Type v:values) if(null != v)m|=v.mask;
			return m;
		}
		public static int sum(Collection<Type>values){
			// 过滤掉null元素
			return null == values ? 0 :sum(Collections2.filter(values, new Predicate<Type>(){
				@Override
				public boolean apply(Type input) {
					return null !=input;
				}}).toArray(new Type[0]));
		}
		public static int sumOfString(Collection<String>values){
			return null == values ? 0 :sum(Collections2.transform(values, new Function<String,Type>(){
				@Override
				public Type apply(String input) {
					try{
						return Type.valueOf(input);
					}catch(Exception e){
						return null;
					}
				}}));
		}
		public static List<Type>checkAll(int value){
			ArrayList<Type> list = new ArrayList<Type>();
			for(Type v: Type.values()){
				if(v.check(value) && Integer.bitCount(v.mask) == 1)
					list.add(v);
			}
			return list;
		}
	}
	public static enum Action{
		ADD,OVERWRITE,APPEND
	}
	public static enum AddColumn{
		ACTION,SCOPE
	}
	public final ClassDoc classDoc;
	/** 缩进字符串 */
	private static String indent = "    ";
	/** 输出comment时要排除的{@link Tag}名字,比如{@code '@throws'} */
	private final Map<String,Integer> excludeTags=Collections.synchronizedMap(new HashMap<String,Integer>());
	/** 输出comment时要添加在commentText中的内容 */
	private final HashBasedTable<String,AddColumn,Object> additionalTextTable = HashBasedTable.create();
	public ExtClassDoc(ClassDoc classDoc) {
		super();
		this.classDoc = Preconditions.checkNotNull(classDoc, "classDoc is null");
	}

//	private static String signature(Method method){
//		return Joiner.on(", ").join(
//				Iterables.transform(Lists.newArrayList(method.getParameterTypes()), new Function<Class<?>, String>() {
//					@Override
//					public String apply(Class<?> input) {
//						return input.getName();
//					}
//				}));
//	}
//	private static String signature(MethodDocImpl method){
//		return  method.signature().replaceAll("\\((.*)\\)", "$1");
//	}
//	public MethodDocImpl getMethodDoc(Method method) {
//		if (null == method)
//			return null;
//		String paramTypes = signature(method);
//		for (MethodDoc m : classDoc.methods()) {
//			MethodDocImpl com.ra.apidoc.doc.doc = (MethodDocImpl) m;
//			if (!method.getName().equals(com.ra.apidoc.doc.doc.name()))continue;
//			String signature = signature(com.ra.apidoc.doc.doc);
//			if(paramTypes.equals(signature))
//				return com.ra.apidoc.doc.doc;
//		}
//		return null;
//	}
	/**
	 * 检查两个方法对象的签名是否匹配<br>
	 * @param method
	 * @param doc
	 * @return 不匹配返回-1,匹配返回>=0的值
	 */
	private static int match(Method method, MethodDoc doc) {
		if (!method.getName().equals(doc.name()))return -1;
		Class<?>[] paramTypes = method.getParameterTypes();
		String s1 = doc.signature().replaceAll("\\((.*)\\)", "$1");
		String[] signature = s1.isEmpty()?new String[0]:s1.replace(" ", "").split(",");
		if (paramTypes.length != signature.length)
			return -1;
		int score = 0;
		for (int i = 0; i < paramTypes.length; ++i) {
			if (paramTypes[i].getName().equals(signature[i]))
				score += 2;// 完全匹配 else
			else if (paramTypes[i].getSimpleName().equals(signature[i]))
				score += 1; // 类名匹配
			else {
				score = -1;
				break;
			}
		}
		return score;
	}
	/**
	 * 在{@link ClassDoc}中查找与method匹配的{@link com.sun.tools.javadoc.MethodDocImpl}<br>
	 * @param method  method
	 * @return 没有找则返回{@code null}
	 * @see #indexOf(Method)
	 */
	public MethodDoc getMethodDoc(Method method) {
		int index = indexOf(method);
		return index < 0 ? null : classDoc.methods()[index];
	}
	/**
	 * 在{@link ClassDoc}中查找与name匹配的{@link com.sun.tools.javadoc.FieldDocImpl}<br>
	 * @param name field name
	 * @return 没有找则返回{@code null}
	 */
	public FieldDoc getFieldDoc(String name) {
		for(FieldDoc field:classDoc.fields()){
			if(field.name().equals(name)){
				return field;
			}
		}
		return null;
	}
	public ParamTag paramTagOf(MethodDoc methodDoc, String name){
		try {
			if(!Strings.isNullOrEmpty(name)){
				for(Tag tag:methodDoc.tags("@param")){
					ParamTag paramTag = (ParamTag)tag;
					if(name.equals(paramTag.parameterName())){
						return paramTag;
					}
				}
			}
			return null;
		} catch (AssertionError e) {
			return null;
		}
	}
	/**
	 * 在{@link ClassDoc}中查找与method匹配的{@link com.sun.tools.javadoc.MethodDocImpl}对象的索引<br>
	 * 不采用注释掉的同名方法的原因是直接对签名进行字符串比较的方法不能适应独立运行环境，
	 * 因为MethodDocImpl返回的方法签名中类名并一定是全名
	 * @param method method
	 * @return 返回{@code method}定义方法在{@link ClassDoc#methods()}数组中的索引,没有找则返回 -1
	 */
	public int indexOf(Method method) {
		if (null == method)
			return -1;
		TreeMap<Integer, Integer> matched = new TreeMap<Integer, Integer>();
		int score;
		MethodDoc[] methodDocs = classDoc.methods();
		for (int i = 0 ; i < methodDocs.length ; ++ i ) {
			MethodDoc m =  methodDocs[i];
			score = match(method, m);
			if (score >= 0)
				matched.put(score,  i);
		}
		return matched.isEmpty() ? -1 : matched.lastEntry().getValue();
	}
	/**
	 * 将{@code methods}按{@link ClassDoc} 定义的顺序重新排序
	 * @param methods 方法列表
	 * @return {@code methods}
	 */
	public List<Method> sortByDefined(List<Method>methods) {
		if(null != methods){
			Collections.sort(methods, new Comparator<Method>(){
				@Override
				public int compare(Method arg0, Method arg1) {
					return indexOf(arg0) - indexOf(arg1);
				}});
		}
		return methods;
	}
	/**
	 * 返回{@code methods}在{@link ClassDoc} 定义顺序索引,{@code methods}为{@code null}时返回空map
	 * @param methods
	 * @return
	 */
	public Map<Method, Integer> indexOf(List<Method>methods) {
		if(null != methods){
			return Maps.asMap(Sets.newHashSet(methods), new Function<Method,Integer>(){
				@Override
				public Integer apply(Method input) {
					return indexOf(input);
				}});
		}
		return ImmutableMap.of();
	}
	public void output(PrintStream out) {
		out.println(formatComment(classDoc, false));
		out.println(classDoc);
		for (MethodDoc method : classDoc.methods()) {
			out.println(formatComment(method, true));
			out.printf("%s%s\n", indent, method.toString());
		}
	}

	public String output() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		output(new PrintStream(out));
		return out.toString();
	}

	private static final String commentBody = "/**" + NEW_LINE + " */" + NEW_LINE;
	private  static final Type typeOfDoc(Object doc){
		if(doc instanceof ClassDoc){
			return Type.CLASS;
		}
		if(doc instanceof MethodDoc){
			return Type.METHOD;
		}
		if(doc instanceof FieldDoc){
			return Type.FIELD;
		}
		throw new UnsupportedOperationException();
	}

	private Set<String> select(final Type type,final Action action){
		return Maps.filterValues(additionalTextTable.rowMap(), new Predicate<Map<AddColumn, Object>>(){
			@Override
			public boolean apply(Map<AddColumn, Object> input) {
				return (input.get(AddColumn.ACTION)==action)&& type.check((Integer)input.get(AddColumn.SCOPE));
			}}).keySet();
	}
	private final String commentText(StringBuffer buffer,String originText,Type type){
		Set<String> addText = select(type, Action.ADD);
		Set<String> overwriteText = select(type, Action.OVERWRITE);
		Set<String> appendText = select(type, Action.APPEND);
		for(String text:addText)buffer.append(text).append(NEW_LINE);
		for(String text:overwriteText)buffer.append(text).append(NEW_LINE);
		if(overwriteText.isEmpty() && !originText.isEmpty())
			buffer.append(originText).append(NEW_LINE);
		for(String text:appendText)buffer.append(text).append(NEW_LINE);
		return buffer.toString();
	}
	/**
	 * 输出格式化的注释信息
	 *
	 * @param doc
	 *            {@link com.sun.tools.javadoc.ClassDocImpl} 或 {@link com.sun.tools.javadoc.MethodDocImpl}实例
	 * @param needIndent
	 *            是否缩进
	 * @return
	 */
	public final String formatComment(Doc doc, boolean needIndent) {
		Preconditions.checkNotNull(doc, "com.ra.apidoc.doc.doc is null");
		Type type = typeOfDoc(doc);
		StringBuffer buffer = new StringBuffer();
		commentText(buffer,doc.commentText(),type);
		for (Tag tag : doc.tags()) {
			if( ! type.check(excludeTags.get(tag.name())) )
				buffer.append(tag.name()).append(" ").append(tag.text()).append(NEW_LINE);
		}
		String cmt = buffer.toString();
		if (!cmt.isEmpty()) {
			cmt = Pattern.compile("(\r\n|\n|\r)\\s*", Pattern.MULTILINE).matcher(cmt).replaceAll(NEW_LINE);
			cmt = Pattern.compile("^", Pattern.MULTILINE).matcher(cmt).replaceAll(" * ");
			cmt = commentBody.replaceFirst(NEW_LINE, "$0" + cmt);
			if (needIndent)
				cmt = Pattern.compile("^", Pattern.MULTILINE).matcher(cmt).replaceAll(indent);
		}
		return cmt;
	}

	public String getClassComment() {
		return formatComment(classDoc, false);
	}

	public String getMethodComment(Method method) {
		MethodDoc doc = getMethodDoc(method);
		return null == doc? null :formatComment(doc, true);
	}
	public String getFieldComment(String name) {
		FieldDoc doc = getFieldDoc(name);
		return null == doc? null :formatComment(doc, true);
	}
	/**
	 * @return indent
	 */
	public static String getIndent() {
		return indent;
	}

	/**
	 * @param indent
	 *            要设置的 indent
	 */
	public static void setIndent(String indent) {
		if (null != indent)
			ExtClassDoc.indent = indent;
	}

	/**
	 * @param excludeTags 要设置的 excludeTags
	 */
	public synchronized void setExcludeTags(Map<String,Integer> excludeTags) {
		if(null != excludeTags){
			excludeTags.clear();
			excludeTags .putAll(excludeTags);
		}
	}
	public void addExcludeTag(String excludeTag,Integer scope) {
		if(!Strings.isNullOrEmpty(excludeTag)){
			excludeTags.put(excludeTag,null ==scope? Type.ALL.mask:scope);
		}
	}
	public void removeExcludeTag(String excludeTag){
		excludeTags.remove(excludeTag);
	}
	public void addExcludeTag(String excludeTag,Type type) {
		addExcludeTag(excludeTag,(null ==type? Type.ALL:type).mask);
	}
	public void addExcludeTag(String excludeTag,Collection<Type> type) {
		addExcludeTag(excludeTag, Type.sum(type));
	}
	public void addExcludeTag(String excludeTag,String type) {
		addExcludeTag(excludeTag, Strings.isNullOrEmpty(type)? Type.ALL: Type.valueOf(type));
	}
	public void addExcludeTagString(String excludeTag,Collection<String> type) {
		if(null == type || type.isEmpty()){
			addExcludeTag(excludeTag, Type.ALL);
		}
		addExcludeTag(excludeTag, Type.sumOfString(type));
	}
	public void addExcludeTag(String excludeTag) {
		addExcludeTag(excludeTag, Type.ALL);
	}
	public void addExcludeTags(Map<String,Integer> excludeTag) {
		if(null != excludeTag)
			excludeTags.putAll(excludeTag);
	}

	public void additionalText(String text,Action action, Integer scope) {
		if(!Strings.isNullOrEmpty(text)){
			additionalTextTable.put(text, AddColumn.ACTION, action);
			additionalTextTable.put(text, AddColumn.SCOPE, scope);
		}
	}
	public void additionalText(String text,Action action, Type... type) {
		additionalText(text,action, (null ==type? Type.ALL.mask: Type.sum(type)));
	}
	public void additionalText(String text,String action, String type) {
		additionalText(text,
				Strings.isNullOrEmpty(action)? Action.ADD: Action.valueOf(action),
				null ==type? Type.ALL: Type.valueOf(type));
	}
	public void additionalText(String text,Action action, Collection<Type> type) {
		additionalText(text,action, (null ==type? Type.ALL.mask: Type.sum(type)));
	}
	public void additionalText(String text,String action, Collection<String> type) {
		additionalText(text,
				Strings.isNullOrEmpty(action)? Action.ADD: Action.valueOf(action),
				null ==type? Type.ALL.mask: Type.sumOfString(type));
	}

	/**
	 * @return classDoc
	 */
	public ClassDoc getClassDoc() {
		return classDoc;
	}
}
