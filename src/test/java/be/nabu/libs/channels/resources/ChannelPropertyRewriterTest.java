package be.nabu.libs.channels.resources;

import java.util.List;

public class ChannelPropertyRewriterTest {
	
	public static void main(String...args) {
		new ChannelPropertyRewriterTest().testRewrite();
		new ChannelPropertyRewriterTest().testListRewrite();
	}
	
	public void testRewrite() {
		ChannelPropertyRewriter rewriter = new ChannelPropertyRewriter();
		TestProperties properties = new TestProperties();
		properties.setTestValue("test/${format(now() - '2m' + '1d', 'yyyy/MM/dd')}/dir");
		List<TestProperties> rewrite = rewriter.rewrite(properties);
		System.out.println(rewrite);
	}
	
	public void testListRewrite() {
		ChannelPropertyRewriter rewriter = new ChannelPropertyRewriter();
		TestProperties properties = new TestProperties();
		properties.setTestValue("test/${range(now() - '2y', now(), '1y', 'yyyy')}/${range(now() - '2d', now(), '1d', 'MM/dd')}/dir");
		properties.setSecondTestValue("fixed");
		properties.setThirdTestValue("test/${format(now(), 'yyyy/MM/dd')}");
		List<TestProperties> rewrite = rewriter.rewrite(properties);
		System.out.println(rewrite);
	}
	
	public static class TestProperties {
		private String testValue, secondTestValue, thirdTestValue;

		public String getTestValue() {
			return testValue;
		}

		public void setTestValue(String testValue) {
			this.testValue = testValue;
		}
		
		@Override
		public String toString() {
			return testValue;
		}

		public String getSecondTestValue() {
			return secondTestValue;
		}

		public void setSecondTestValue(String secondTestValue) {
			this.secondTestValue = secondTestValue;
		}

		public String getThirdTestValue() {
			return thirdTestValue;
		}

		public void setThirdTestValue(String thirdTestValue) {
			this.thirdTestValue = thirdTestValue;
		}
	}
}
